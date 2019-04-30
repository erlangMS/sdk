/*
 * 
 */
package br.erlangms.rest.request;

import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.schema.RestField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe de implementação da IRestApiRequest
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public final class RestApiRequest implements IRestApiRequest {
	private static final long serialVersionUID = -6902253220018384046L;
	public static final Integer REST_API_MAX_LIMIT_VALUE = 1000;
    public static final Integer REST_API_DEFAULT_LIMIT_VALUE = 100;
    public static final Long REST_API_MAX_ID_VALUE = 99999999999999L;
    private int hashCodeComputed = 0;
    private IRestApiProvider apiProvider;
    private final boolean isDefault;
    private String filter;
    private RestFilterAST filterAST;
    private String fields;
    private List<RestField> fieldsList;
    private String sort;
    private List<RestField> sortList;
    private Integer limit;
    private Integer offset;
    private Integer maxLimit;
    private Long maxId;
    private Long id;
    private RestApiDataFormat dataFormat;
    private RestApiRequestState state;
    private final IRestApiRequestFlags flags;
    private final IRestApiRequestStatistics statistics;


    /*
     * Este construtor privado é utilizado exclusivamente para criar uma instancia default do request
     * para validação da requisição.
     *
     */
    private RestApiRequest(final IRestApiProvider apiProvider, boolean isDefault) {
        this.flags = new RestApiRequestFlags();
        this.statistics = new RestApiRequestStatistics();
        this.state = RestApiRequestState.OPEN;
        this.isDefault = isDefault;
        this.apiProvider = apiProvider;
        this.filter = null;
        this.filterAST = null;
        if (apiProvider.__getContract() != null) {
            this.fields = String.join(",", apiProvider.getContract().getSchema().getVoFields());
            this.fieldsList = new ArrayList<>(apiProvider.getContract().getSchema().getFieldsList());
        } else {
            this.fields = null;
            this.fieldsList = new ArrayList<>();
        }
        this.sort = null;
        this.sortList = new ArrayList<>();
        this.maxLimit = REST_API_MAX_LIMIT_VALUE;
        this.limit = REST_API_DEFAULT_LIMIT_VALUE;
        this.offset = 0;
        this.id = null;
        this.maxId = REST_API_MAX_ID_VALUE;
        this.dataFormat = RestApiDataFormat.VO;
        this.hashCodeComputed = 0;
        this.state = RestApiRequestState.PARSED;
    }

    public RestApiRequest() {
        this.state = RestApiRequestState.OPEN;
        this.isDefault = false;
        this.flags = new RestApiRequestFlags();
        this.statistics = new RestApiRequestStatistics();
        setApiProvider(null);
        setFilter(null);
        setFields(null);
        setSort(null);
        setMaxLimit(null); // deve vir antes de setLimit
        setLimit(null);
        setOffset(null);
        setId(null);
        setDataFormat(null);
        this.hashCodeComputed = 0;
    }

    public RestApiRequest(final IRestApiProvider apiProvider) {
        this.state = RestApiRequestState.OPEN;
        this.isDefault = false;
        this.flags = new RestApiRequestFlags();
        this.statistics = new RestApiRequestStatistics();
        setApiProvider(apiProvider);
        setFilter(null);
        setFields(null);
        setSort(null);
        setMaxLimit(null); // deve vir antes de setLimit
        setLimit(null);
        setOffset(null);
        setId(null);
        setDataFormat(null);
        this.hashCodeComputed = 0;
    }

    public static IRestApiRequest createDefaultRequest(final IRestApiProvider apiProvider) {
        IRestApiRequest result = new RestApiRequest(apiProvider, true);
        return result;
    }

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(final String filter) {
        switch (state) {
            case OPEN:
                this.filter = filter;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                if (filter != null && !filter.isEmpty() && !filter.equals("{}")) {
                    this.filterAST = RestApiRequestParser.parseFilterAST(filter, getApiProvider());
                } else {
                    this.filterAST = null;
                }
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public String getFields() {
        return fields;
    }

    @Override
    public void setFields(final String fields) {
        switch (state) {
            case OPEN:
                this.fields = fields;
                this.fieldsList = null;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                this.fieldsList = RestApiRequestParser.parseFields(fields, apiProvider);
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public List<RestField> getFieldsList() {
        return fieldsList;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public void setSort(final String sort) {
        switch (state) {
            case OPEN:
                this.sort = sort;
                this.sortList = null;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                this.sortList = RestApiRequestParser.parseSort(sort, getApiProvider());
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public List<RestField> getSortList() {
        return sortList;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void setLimit(final Integer limit) {
        switch (state) {
            case OPEN:
                this.limit = limit;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                if (limit != null) {
                    if (!(limit > 0 && limit <= maxLimit)) {
                        throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_LIMIT_FORA_INTERVALO_PERMITIDO);
                    }
                } else {
                    this.limit = apiProvider.getContract().getRequestDefault().getLimit();
                }
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public void setOffset(final Integer offset) {
        switch (state) {
            case OPEN:
                this.offset = offset;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                if (offset != null) {
                    if (!(offset >= 0 && offset <= maxLimit)) {
                        throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_OFFSET_FORA_INTERVALO_PERMITIDO);
                    }
                }
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public Integer getMaxLimit() {
        return maxLimit;
    }

    @Override
    public void setMaxLimit(final Integer maxLimit) {
        switch (state) {
            case OPEN:
                this.maxLimit = this.limit;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                if (maxLimit != null) {
                    this.maxLimit = maxLimit;
                    if (this.limit > this.maxLimit) {
                        this.maxLimit = this.limit;
                    }
                }
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        switch (state) {
            case OPEN:
                this.id = id;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                if (id != null) {
                    if (!(id >= 0 && id <= maxId)) {
                        throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_ID_FORA_INTERVALO_PERMITIDO);
                    }
                }
                break;
            case PARSED:
                interruptChange();
        }
    }

    @Override
    public void setIdAsString(String idStr) {
        if (idStr != null) {
            try {
                idStr = idStr.trim();
                setId(Long.parseUnsignedLong(idStr));
            } catch (Exception ex) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_ID_NAO_EH_INTEIRO_MAIOR_QUE_ZERO);
            }
        } else {
            this.id = null;
        }
    }

    @Override
    public Long getMaxId() {
        return maxId;
    }

    @Override
    public void setMaxId(final Long maxId) {
        switch (state) {
            case OPEN:
                this.maxId = maxId;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                break;
            case PARSED:
                interruptChange();
        }
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
    public void setApiProvider(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    @Override
    public RestApiDataFormat getDataFormat() {
        return this.dataFormat;
    }

    @Override
    public void setDataFormat(RestApiDataFormat dataFormat) {
        switch (state) {
            case OPEN:
                this.dataFormat = dataFormat;
                this.hashCodeComputed = 0;
                break;
            case PARSING:
                break;
            case PARSED:
                interruptChange();
        }
    }

    public void setDataFormatAsString(final String dataFormatStr) {
        if (dataFormatStr != null) {
            setDataFormat(RestApiDataFormat.strToEnum(dataFormatStr));
        } else {
            this.dataFormat = null;
        }
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public int hashCode() {
        if (hashCodeComputed == 0) {
            int hash = 3;
            // O hash code vai utilizar somente este atributos
            hash = hash + Objects.hashCode(this.filter);
            hash = hash + Objects.hashCode(this.fields);
            hash = hash + Objects.hashCode(this.sort);
            hash = hash + Objects.hashCode(this.limit);
            hash = hash + Objects.hashCode(this.offset);
            hash = hash + Objects.hashCode(this.id);
            hash = hash + Objects.hashCode(this.dataFormat);
            hashCodeComputed = hash;
        }
        return hashCodeComputed;
    }

    @Override
    public void parse() {
        if (apiProvider == null) {
            throw new RestApiConstraintException(RestApiConstraintException.REQUISICAO_PRECISA_PROVIDER_PARA_PARSE);
        }

        if (!isParsed()) {
            state = RestApiRequestState.PARSING;

            // Para validar é necessário fazer o parser dos operadores
            setDataFormat(dataFormat);
            setFilter(filter);
            setFields(fields);
            setSort(sort);
            setMaxLimit(maxLimit);
            setLimit(limit);
            setMaxId(maxId);
            setId(id);

            // Além disso, precisa ver se não viola alguma constraint do provider
            getApiProvider().validateRequestWithConstraints(this);

            state = RestApiRequestState.PARSED;
        }
    }

    @Override
    public void open() {
        this.state = RestApiRequestState.OPEN;
        this.hashCodeComputed = 0;
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
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    private void interruptChange() {
        // não levanta exception para request default!!!
        if (!isDefault) {
            throw new RestApiConstraintException(RestApiConstraintException.REQUISICAO_SO_PODE_SER_ALTERADA_SE_INVALIDADA);
        }
    }

    @Override
    public void __setFields(String fields) {
        this.fields = fields;
    }

    @Override
    public void __setFieldsList(ArrayList<RestField> fieldsList) {
        this.fieldsList = fieldsList;
    }

    @Override
    public void setDefaultsIfEmpty() {
        if (state == RestApiRequestState.OPEN) {
            if (apiProvider != null) {
                // Obs.: this.hashCodeComputed Ã© definido como zero somente para os campos que estão no hashCode

                if (dataFormat == null) {
                    this.dataFormat = apiProvider.getContract().getRequestDefault().getDataFormat();
                    this.hashCodeComputed = 0;
                }
                if (fields == null || fields.isEmpty()) {
                    this.fieldsList = new ArrayList<>(apiProvider.getContract().getRequestDefault().getFieldsList());
                    this.fields = apiProvider.getContract().getRequestDefault().getFields();
                    this.hashCodeComputed = 0;
                }
                if (maxId == null) {
                    this.maxId = apiProvider.getContract().getRequestDefault().getMaxId();
                }
                if (maxLimit == null) {
                    this.maxLimit = apiProvider.getContract().getRequestDefault().getMaxLimit();
                }
                if (sort == null || sort.isEmpty()) {
                    this.sortList = new ArrayList<>(apiProvider.getContract().getRequestDefault().getSortList());
                    this.sort = apiProvider.getContract().getRequestDefault().getSort();
                    this.hashCodeComputed = 0;
                }
                if (offset == null) {
                    this.offset = apiProvider.getContract().getRequestDefault().getOffset();
                    this.hashCodeComputed = 0;
                }
                if (limit == null) {
                    this.limit = apiProvider.getContract().getRequestDefault().getLimit();
                    this.hashCodeComputed = 0;
                }
            } else {
                throw new RestApiConstraintException(RestApiConstraintException.NECESSARIO_INFORMAR_PROVIDER);
            }
        } else {
            interruptChange();
        }
    }

    @Override
    public IRestApiRequestStatistics getStatistics() {
        return statistics;
    }

    @Override
    public IRestApiRequestFlags getFlags() {
        return flags;
    }

    @Override
    public void setFlagsAsString(String flags) {
        getFlags().setFlagsAsString(flags);
    }

    @Override
    public String toString() {
        return "RestApiRequest{ hashCode=" + hashCode() + ", filter=" + filter + ", fields=" + fields + ", sort=" + sort + ", limit=" + limit + ", offset=" + offset + ", id=" + id + ", dataFormat=" + dataFormat + ", statistics=" + statistics + '}';
    }

}
