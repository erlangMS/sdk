package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.contract.RestApiVerb;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.filter.RestFilterCondition;
import br.unb.erlangms.rest.filter.RestFilterFindByNameVisitor;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.schema.RestField;
import java.util.List;
import java.util.Optional;

/**
 * Classe de implementação da IRestApiRequestInternal
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public final class RestApiRequestInternal implements IRestApiRequestInternal {
    private final IRestApiRequest requestUser;
    private final IRestApiProvider apiProvider;
    private final boolean isDefault;
    private final int RID;
    private final RestApiVerb apiVerb;
    private RestFilterAST filterAST;
    private List<RestField> fieldsList;
    private List<RestField> sortList;
    private RestApiRequestState state;

    public RestApiRequestInternal(final IRestApiRequest requestUser, final IRestApiProvider apiProvider, final RestApiVerb apiVerb) {
        this.state = RestApiRequestState.OPEN;
        this.isDefault = false;
        this.requestUser = requestUser;
        this.apiProvider = apiProvider;
        this.RID = requestUser.hashCode();
        this.apiVerb = apiVerb;
    }

    @Override
    public List<RestField> getFieldsList() {
        return fieldsList;
    }

    @Override
    public List<RestField> getSortList() {
        return sortList;
    }

    @Override
    public RestFilterAST getFilterAST() {
        return filterAST;
    }

    @Override
    public IRestApiProvider getApiProvider() {
        return apiProvider;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public void parse() {
        if (apiProvider == null) {
            throw new RestApiException(RestApiException.REQUISICAO_PRECISA_PROVIDER_PARA_PARSE);
        }

        if (!isParsed()) {
            state = RestApiRequestState.PARSING;

            // Para validar é necessário fazer o parser dos operadores
            RestApiDataFormat dataFormat = requestUser.getDataFormat();
            if (dataFormat == null) {
                dataFormat = apiProvider.getContract().getRequestDefault().getDataFormat();
                if (dataFormat == null) {
                    dataFormat = RestApiDataFormat.VO;
                }
                requestUser.setDataFormat(dataFormat);
            }

            String filter = requestUser.getFilter();
            if (filter != null && !filter.isEmpty() && !filter.equals("{}")) {
                this.filterAST = RestApiRequestParser.parseFilterAST(filter, getApiProvider());
            }

            String fields = requestUser.getFields();
            if (fields != null && !fields.isEmpty()) {
                if (dataFormat == RestApiDataFormat.ENTITY) {
                    throw new RestApiException(RestApiException.FORMATO_ENTITY_NAO_SUPORTA_OPERADOR_FIELS);
                }
                this.fieldsList = RestApiRequestParser.parseFields(fields, apiProvider);
            } else {
                if (dataFormat != RestApiDataFormat.ENTITY) {
                    fields = apiProvider.getContract().getRequestDefault().getFields();
                    if (fields == null || fields.isEmpty()) {
                        fields = apiProvider.getContract().getSchema().getVoFields();
                    }
                    requestUser.setFields(fields);
                    this.fieldsList = RestApiRequestParser.parseFields(fields, apiProvider);
                }
            }

            String sort = requestUser.getSort();
            if (sort == null || sort.isEmpty()) {
                sort = apiProvider.getContract().getRequestDefault().getSort();
                requestUser.setSort(sort);
            }
            this.sortList = RestApiRequestParser.parseSort(requestUser.getSort(), getApiProvider());

            Integer maxLimit = requestUser.getMaxLimit();
            if (maxLimit == null) {
                maxLimit = apiProvider.getContract().getRequestDefault().getMaxLimit();
                if (maxLimit == null) {
                    maxLimit = RestApiRequest.REST_API_MAX_LIMIT_VALUE;
                }
                requestUser.setMaxLimit(maxLimit);
            }

            Integer limit = requestUser.getLimit();
            if (limit == null) {
                limit = apiProvider.getContract().getRequestDefault().getLimit();
                if (limit == null) {
                    limit = RestApiRequest.REST_API_DEFAULT_LIMIT_VALUE;
                }
                requestUser.setLimit(limit);
            }
            if (!(limit > 0 && limit <= requestUser.getMaxLimit())) {
                throw new RestApiException(RestApiException.OPERADOR_LIMIT_FORA_INTERVALO_PERMITIDO);
            }

            Long maxId = requestUser.getMaxId();
            if (maxId == null) {
                maxId = apiProvider.getContract().getRequestDefault().getMaxId();
                if (maxId == null) {
                    maxId = RestApiRequest.REST_API_MAX_ID_VALUE;
                }
                requestUser.setMaxId(maxId);
            }

            Long id = requestUser.getId();
            if (id != null) {
                if (!(id >= 0 && id <= requestUser.getMaxId())) {
                    throw new RestApiException(RestApiException.OPERADOR_ID_FORA_INTERVALO_PERMITIDO);
                }
            }

            Integer offset = requestUser.getOffset();
            if (offset == null) {
                offset = apiProvider.getContract().getRequestDefault().getOffset();
                if (offset == null) {
                    offset = RestApiRequest.REST_API_DEFAULT_OFFSET_VALUE;
                }
                requestUser.setOffset(offset);
            }
            if (!(offset >= 0 && offset <= maxLimit)) {
                throw new RestApiException(RestApiException.OPERADOR_OFFSET_FORA_INTERVALO_PERMITIDO);
            }

            // Além disso, precisa ver se não viola alguma constraint do provider
            getApiProvider().validateRequestWithContract(this);

            state = RestApiRequestState.PARSED;
        }
    }

    @Override
    public void open() {
        this.state = RestApiRequestState.OPEN;
    }

    @Override
    public boolean isParsed() {
        return this.state == RestApiRequestState.PARSED;
    }

    @Override
    public RestApiRequestState getState() {
        return state;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    @Override
    public String toString() {
        return requestUser.toString();
    }

    @Override
    public IRestApiRequest getRequestUser() {
        return requestUser;
    }

    @Override
    public String getFields() {
        return requestUser.getFields();
    }

    @Override
    public String getFilter() {
        return requestUser.getFilter();
    }

    @Override
    public Long getId() {
        return requestUser.getId();
    }

    @Override
    public Integer getLimit() {
        return requestUser.getLimit();
    }

    @Override
    public Long getMaxId() {
        return requestUser.getMaxId();
    }

    @Override
    public Integer getMaxLimit() {
        return requestUser.getMaxLimit();
    }

    @Override
    public Integer getOffset() {
        return requestUser.getOffset();
    }

    @Override
    public String getSort() {
        return requestUser.getSort();
    }

    @Override
    public RestApiDataFormat getApiDataFormat() {
        return requestUser.getDataFormat();
    }

    @Override
    public IRestApiRequestFlags getFlags() {
        return requestUser.getFlags();
    }

    @Override
    public int hashCode() {
        return requestUser.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return requestUser.equals(obj);
    }

    @Override
    public int getRID() {
        return RID;
    }

    @Override
    public Optional<RestFilterCondition> findConditionByVoFieldName(final RestField field) {
        if (filterAST != null) {
            RestFilterFindByNameVisitor visitor = new RestFilterFindByNameVisitor(field);
            filterAST.visit(visitor);
            Optional<RestFilterCondition> result = Optional.ofNullable(visitor.getResult());
            return result;
        }
        return Optional.empty();
    }

    public RestApiVerb getApiVerb() {
        return apiVerb;
    }



}
