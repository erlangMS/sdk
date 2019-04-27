package br.erlangms.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Parameter;
import javax.persistence.Query;

import br.erlangms.rest.cache.IRestApiCacheEntry;
import br.erlangms.rest.cache.IRestApiCachePolicyConfig;
import br.erlangms.rest.cache.RestApiCacheManager;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.exception.RestApiException;
import br.erlangms.rest.exception.RestApiNotFoundException;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.provider.RestApiProviderFactory;
import br.erlangms.rest.query.IRestQueryGenerator;
import br.erlangms.rest.query.RestJpaQueryParameterSetter;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.request.IRestApiRequestFlags;
import br.erlangms.rest.request.IRestApiRequestStatistics;
import br.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.erlangms.rest.serializer.RestApiSerializerFactory;

/**
 * Implementa IRestApiManager e fornece acesso a camada de abstração REST.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 27/03/2019
 *
 */
public abstract class RestApiManager implements IRestApiManager {
	private static final long serialVersionUID = 6759845862361236309L;
	private static final Logger LOGGER = Logger.getLogger(RestApiManager.class.getName());

    private Query createQuery(final IRestApiRequest request, final IRestApiProvider apiProvider) throws RestApiException {
        if (request != null) {
            Query query;
            if (apiProvider.getContract().isSupportNamedQuery() && !request.getFlags().isNoNamedQuery()) {
                String queryName = "RestApiQ" + String.valueOf(request.hashCode());
                try {
                    query = getEntityManager().createNamedQuery(queryName);
                } catch (Exception ex) {
                    apiProvider.setApiManager(this);
                    IRestQueryGenerator queryGenerator = apiProvider.createQueryGenerator();
                    query = queryGenerator.createQuery(request);
                    getEntityManager().getEntityManagerFactory().addNamedQuery(queryName, query);
                }
            } else {
                apiProvider.setApiManager(this);
                IRestQueryGenerator queryGenerator = apiProvider.createQueryGenerator();
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
        } else {
            throw new RestApiConstraintException(RestApiConstraintException.REQUEST_OBRIGATORIO);
        }
    }

    @Override
    @SuppressWarnings("null")
    public Object find(IRestApiRequest request, final Class apiProviderClass) throws RestApiException {
        if (request != null) {
            try {
                IRestApiProvider apiProvider = RestApiProviderFactory.createInstance(apiProviderClass);
                request.setApiProvider(apiProvider);
                IRestApiCachePolicyConfig cachePolicyConfig = apiProvider.getContract().getCachePolicyConfig();
                IRestApiCacheEntry cacheEntry;
                Object data;
                Object result;

                // Setar os valores default para todos os operadores vazios
                request.setDefaultsIfEmpty();

                IRestApiRequestStatistics stat = request.getStatistics();
                stat.setStartTime(System.currentTimeMillis());

                IRestApiRequestFlags flags = request.getFlags();

                // Se a requisição pode ser cacheada, tenta obter ele do subsistema de cache
                if (cachePolicyConfig.isAllowRequestCache() && !flags.isNoRequestCache() && !flags.isNoCache()) {
                    cacheEntry = RestApiCacheManager.get(request);
                } else {
                    cacheEntry = null;
                }

                if (cacheEntry == null || cacheEntry.isEmpty()) {
                    // Fazer o parser e validação
                    request.parse();

                    // Pode ser que a query esteja no pool de named query
                    Query query = createQuery(request, apiProvider);

                    // Aqui que realmente vai executar a consulta no banco de dados
                    try {
                        data = query.getResultList();
                    } catch (Exception ex) {
                        throw new RestApiException(RestApiConstraintException.ERRO_OBTER_DADOS_QUERY);
                    }

                    // Realiza a serialização
                    IRestApiSerializerStrategy serializer = RestApiSerializerFactory.createInstance(request.getDataFormat());
                    serializer.execute(request, apiProvider, data);
                    result = serializer.getData();
                    Integer estimatedSize = serializer.getEstimatedSize();

                    // Cachear o resultado ajuda muito a performance
                    if (!flags.isNoCache()
                        && !flags.isNoResultCache()
                        && cachePolicyConfig.isAllowResultCache()
                        && estimatedSize != null
                        && estimatedSize <= cachePolicyConfig.getEntrySizeBytes()) {
                        cacheEntry.setData(result);
                    }
                } else {
                    result = cacheEntry.getData();
                    request = cacheEntry.getRequest();  
                }

                // Simular lentidão
                if (flags.isSlowTest()) {
                    try {
                        Thread.sleep((long) (Math.random() * 1000));
                    } catch (InterruptedException ex2) {
                        // acordou do slow test!!!
                    }
                }

                stat.setStopTime(System.currentTimeMillis());
                if (flags.isLogStatistics()) {
                    LOGGER.log(Level.INFO, "{0}", request.toString());
                }

                return result;
            } catch (RestApiException ex) {
                throw new RestApiConstraintException(String.format(RestApiConstraintException.ERRO_EXECUTA_FIND, ex.getMessage()));
            }
        } else {
            throw new RestApiConstraintException(RestApiConstraintException.REQUEST_OBRIGATORIO);
        }
    }

    @Override
    public Object findById(final IRestApiRequest request, final Class apiProviderClass) throws RestApiNotFoundException {
        if (request != null) {
            if (request.getId() != null) {
                if (request.getDataFormat() == RestApiDataFormat.ENTITY) {
                    return getEntityManager().find(request.getApiProvider().getEntityClass(), request.getId());
                } else {
                    request.setFilter("{\"id\":\"" + String.valueOf(request.getId()) + "\"}");
                    return find(request, apiProviderClass);
                }
            } else {
                throw new RestApiConstraintException(RestApiConstraintException.REQUEST_COM_ID_OBRIGATORIO);
            }
        } else {
            throw new RestApiConstraintException(RestApiConstraintException.REQUEST_OBRIGATORIO);
        }
    }

}
