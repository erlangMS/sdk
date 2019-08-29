package br.unb.erlangms.rest;

import br.unb.erlangms.rest.cache.IRestApiCacheEntry;
import br.unb.erlangms.rest.cache.IRestApiCachePolicyConfig;
import br.unb.erlangms.rest.cache.RestApiCacheManager;
import br.unb.erlangms.rest.cache.RestApiCacheProvider;
import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.contract.RestApiVerb;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.exception.RestApiNotFoundException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.provider.RestApiProviderFactory;
import br.unb.erlangms.rest.query.IRestQueryGenerator;
import br.unb.erlangms.rest.query.RestJpaQueryParameterSetter;
import br.unb.erlangms.rest.request.IRestApiRequest;
import br.unb.erlangms.rest.request.IRestApiRequestFlags;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import br.unb.erlangms.rest.request.RestApiRequestInternal;
import br.unb.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.unb.erlangms.rest.serializer.RestApiSerializerFactory;
import br.unb.erlangms.rest.util.RestUtils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;

/**
 * Implementa IRestApiManager e fornece acesso a camada de abstração REST.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 27/03/2019
 *
 */
public abstract class RestApiManager implements IRestApiManager {

    private static final long serialVersionUID = 4975518654944032325L;
    private static final Logger LOGGER = Logger.getLogger(RestApiManager.class.getName());

    private Query createQuery(final IRestApiRequestInternal request, final IRestApiProvider apiProvider) throws RestApiException {
        Query query;
        IRestApiRequestFlags flags = request.getRequestUser().getFlags();
        IRestQueryGenerator queryGenerator = apiProvider.createQueryGenerator();
        EntityManager entityManager = getEntityManager();

        if (apiProvider.getContract().isSupportNamedQuery() && !(flags.isNoNamedQuery() || flags.isNoCache())) {
            String queryName = "RestApiQ" + String.valueOf(request.hashCode());
            try {
                query = entityManager.createNamedQuery(queryName);
            } catch (Exception ex) {
                // Se não estiver no pool de named query, cria a query e depois adiciona
                apiProvider.setApiManager(this);
                query = queryGenerator.createQuery(request);
                entityManager.getEntityManagerFactory().addNamedQuery(queryName, query);
            }
        } else {
            apiProvider.setApiManager(this);
            query = queryGenerator.createQuery(request);
        }

        // Seta todos os parâmetros da query para null
        for (Parameter p : query.getParameters()) {
            query.setParameter(p, null);
        }

        // Seta os parâmetros da query de acordo com o operador filter
        if (request.getFilterAST() != null) {
            if (!query.getParameters().isEmpty()) {
                RestJpaQueryParameterSetter jpaQueryParameterSetter = new RestJpaQueryParameterSetter(request, query);
                request.getFilterAST().visit(jpaQueryParameterSetter);
            }
        }

        return query;
    }

    @Override
    public Object find(IRestApiRequest request, final Class apiProviderClass) {
        if (request != null && apiProviderClass != null) {
            try {
                IRestApiProvider apiProvider = RestApiProviderFactory.createInstance(apiProviderClass);
                IRestApiRequestInternal requestInternal = new RestApiRequestInternal(request, apiProvider, RestApiVerb.GET);
                if (canExecute(request, apiProvider)) {
                    IRestApiCachePolicyConfig cachePolicyConfig = apiProvider.getContract().getCachePolicyConfig();
                    IRestApiCacheEntry cacheEntry;
                    Object data;
                    Object result;

                    IRestApiRequestFlags flags = requestInternal.getRequestUser().getFlags();

                    // Deve simular um teste de lentidão?
                    if (flags.isSlowTest()) {
                        try {
                            Thread.sleep((long) (Math.random() * 1000));
                        } catch (InterruptedException ex2) {
                            // acordou do slow test!!!
                        }
                    }

                    RestApiCacheProvider cacheProvider = RestApiCacheManager.get(apiProvider);
                    cacheEntry = cacheProvider.get(requestInternal);

                    if (cacheEntry.isEmpty()) {
                        cacheEntry.setStartTime(System.currentTimeMillis());

                        // Fazer o parser também vai efetuar a validação
                        requestInternal.parse();

                        // Ao criar a query, é possível que ela já esteja no pool da named query da JPA
                        Query query = createQuery(requestInternal, apiProvider);

                        // Aqui que realmente vai executar a consulta no banco de dados
                        try {
                            data = query.getResultList();
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "{0}", requestInternal.toString());
                            LOGGER.log(Level.SEVERE, ex.getMessage());
                            throw new RestApiException(RestApiException.ERRO_OBTER_DADOS_QUERY);
                        }

                        // A serialização dos dados é uma etapa custosa!!!
                        IRestApiSerializerStrategy serializer = RestApiSerializerFactory.createInstance(requestInternal.getRequestUser().getDataFormat());
                        serializer.execute(requestInternal, apiProvider, data);
                        result = serializer.getData();

                        Integer estimatedSize = serializer.getEstimatedSize();
                        cacheEntry.setEstimatedSize(estimatedSize);

                        // Cachear o resultado é uma opção que ajuda muito a performance mas
                        // só pode ser usado se o serializador tem uma estimativa do tamanho do resultado
                        if (!flags.isNoCache()
                                && !flags.isNoResultCache()
                                && cachePolicyConfig.isAllowResultCache()
                                && estimatedSize != null
                                && estimatedSize <= cachePolicyConfig.getEntrySizeBytes()) {
                            cacheEntry.saveData(result);
                        }

                        cacheEntry.setStopTime(System.currentTimeMillis());
                    } else {
                        result = cacheEntry.getData();
                        requestInternal = cacheEntry.getRequest();  // já que encontrou a requisição, sobrescreve a variável request
                    }

                    if (flags.isLogRequest()) {
                        String statisticsLog = ", RestApiRequestStatistics{"
                                + "rid=" + requestInternal.getRID()
                                + ", startTime=" + cacheEntry.getStartTime()
                                + ", stopTime=" + cacheEntry.getStopTime()
                                + ", elapsedTime=" + cacheEntry.getElapsedTime()
                                + ", expireDate=" + cacheEntry.getExpireDate()
                                + ", estimatedSize=" + cacheEntry.getEstimatedSize()
                                + ", requestCacheHit=" + cacheEntry.getRequestCacheHit()
                                + ", resultCacheHit=" + cacheEntry.getResultCacheHit()
                                + ", entryWatermark=" + cacheEntry.getWatermark()
                                + ", bufferWatermark=" + cacheProvider.getWatermark()
                                + ", circularIndex=" + cacheProvider.getCircularIndex()
                                + '}';
                        LOGGER.log(Level.INFO, "{0}", requestInternal.toString() + statisticsLog);
                    }

                    return result;
                } else {
                    throw new RestApiException(RestApiException.ACCESS_DENIED);
                }
            } catch (RestApiException ex) {
                throw ex;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                throw new RestApiException(RestApiException.ERRO_GET_SERVICE);
            }
        } else {
            throw new RestApiException(RestApiException.REQUEST_E_PROVIDER_OBRIGATORIO);
        }
    }

    @Override
    public Object findById(final IRestApiRequest request, final Class apiProviderClass) throws RestApiNotFoundException {
        if (request != null && apiProviderClass != null) {
            if (request.getId() != null) {
                Object result;
                if (request.getDataFormat() == RestApiDataFormat.ENTITY) {
                    IRestApiProvider apiProvider = RestApiProviderFactory.createInstance(apiProviderClass);
                    result = getEntityManager().find(apiProvider.getEntityClass(), request.getId());
                    if (result == null) {
                        throw new RestApiNotFoundException();
                    }
                } else {
                    request.setFilter("{\"id\":" + String.valueOf(request.getId()) + "}");
                    request.setLimit(1);
                    result = find(request, apiProviderClass);
                    if (result == null
                            || (result instanceof List && ((List) result).isEmpty())
                            || (result instanceof String && ((String) result).isEmpty())) {
                        throw new RestApiNotFoundException();
                    }
                    result = ((List)result).get(0);
                }
                return result;
            } else {
                throw new RestApiException(RestApiException.REQUEST_COM_ID_OBRIGATORIO);
            }
        } else {
            throw new RestApiException(RestApiException.REQUEST_E_PROVIDER_OBRIGATORIO);
        }
    }

    @Override
    public Object put(final IRestApiRequest request, final Class apiProviderClass, final RestApiPersistCallback persistCallback) {
        if (request != null && apiProviderClass != null) {
            if (request.getId() != null) {
                request.setDataFormat(RestApiDataFormat.ENTITY);
                try {
                    IRestApiProvider apiProvider = RestApiProviderFactory.createInstance(apiProviderClass);
                    apiProvider.getContract().checkSupportApiVerb(RestApiVerb.PUT);
                    if (canExecute(request, apiProvider)) {
                        Object obj = findById(request, apiProviderClass);
                        RestUtils.setValuesFromMap(obj, request.getPayloadAsMap(), null, apiProvider);
                        request.setObject(obj);
                        Object objectInserted = null;
                        if (persistCallback != null) {
                            objectInserted = persistCallback.execute();
                        }
                        request.setDataFormat(RestApiDataFormat.VO);
                        RestApiCacheProvider cacheProvider = RestApiCacheManager.get(apiProvider);
                        cacheProvider.clear();
                        if (request.getId() != null && request.getId() > 0) {
                            return findById(request, apiProviderClass);
                        } else {
                            return true;
                        }
                    } else {
                        throw new RestApiException(RestApiException.ACCESS_DENIED);
                    }
                } catch (RestApiException ex) {
                    throw ex;
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                    throw new RestApiException(RestApiException.ERRO_PUT_SERVICE);
                }
            } else {
                throw new RestApiException(RestApiException.REQUEST_COM_ID_OBRIGATORIO);
            }
        } else {
            throw new RestApiException(RestApiException.REQUEST_E_PROVIDER_OBRIGATORIO);
        }
    }

    @Override
    public Object post(final IRestApiRequest request, final Class apiProviderClass, final RestApiPersistCallback persistCallback) {
        if (request != null && apiProviderClass != null) {
            try {
                IRestApiProvider apiProvider = RestApiProviderFactory.createInstance(apiProviderClass);
                apiProvider.getContract().checkSupportApiVerb(RestApiVerb.POST);
                if (canExecute(request, apiProvider)) {
                    Object obj = apiProvider.getEntityClass().newInstance();
                    RestUtils.setValuesFromMap(obj, request.getPayloadAsMap(), null, apiProvider);
                    request.setObject(obj);
                    if (persistCallback != null) {
                        Long idGenerated = persistCallback.execute();
                        request.setId(idGenerated);
                    }
                    request.setDataFormat(RestApiDataFormat.VO);
                    RestApiCacheProvider cacheProvider = RestApiCacheManager.get(apiProvider);
                    cacheProvider.clear();
                    if (request.getId() != null && request.getId() > 0) {
                        return findById(request, apiProviderClass);
                    } else {
                        return true;
                    }
                } else {
                    throw new RestApiException(RestApiException.ACCESS_DENIED);
                }
            } catch (RestApiException ex) {
                throw ex;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                throw new RestApiException(RestApiException.ERRO_POST_SERVICE);
            }
        } else {
            throw new RestApiException(RestApiException.REQUEST_E_PROVIDER_OBRIGATORIO);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canExecute(final IRestApiRequest request, final IRestApiProvider apiProvider) {
        return true;
    }

}
