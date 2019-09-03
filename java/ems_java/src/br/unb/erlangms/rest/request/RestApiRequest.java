package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.provider.RestApiProviderFactory;
import br.unb.erlangms.rest.util.RestUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author evertonagilar
 */
public final class RestApiRequest implements IRestApiRequest {
    private static final long serialVersionUID = -8169941646186654799L;
    public static final Integer REST_API_MAX_LIMIT_VALUE = 1000;
    public static final Integer REST_API_DEFAULT_LIMIT_VALUE = 100;
    public static final Integer REST_API_DEFAULT_OFFSET_VALUE = 0;
    public static final Long REST_API_MAX_ID_VALUE = 999999999L;
    public static final RestApiDataFormat REST_API_DEFAULT_DATA_FORMAT = RestApiDataFormat.VO;
    private String filter;
    private String fields;
    private String sort;
    private Integer limit;
    private Integer offset;
    private Integer maxLimit;
    private Long maxId;
    private Long id;
    private RestApiDataFormat dataFormat;
    private final IRestApiRequestFlags flags;
    private final HashMap<String, Object> paramters;
    private Object payload;     // o objeto como ele chegou do cliente
    private Object object;      // o objeto depois de serializado
    private String authorization;


    public RestApiRequest() {
        this.flags = new RestApiRequestFlags();
        this.paramters = new HashMap<>();
        setFilter(null);
        setFields(null);
        setSort(null);
        setMaxLimit(null); // deve vir antes de setLimit
        setLimit(null);
        setOffset(null);
        setMaxId(null);
        setId(null);
        setDataFormat(REST_API_DEFAULT_DATA_FORMAT);
    }

    @Override
    public String getFields() {
        return fields;
    }

    @Override
    public void setFields(final String fields) {
        this.fields = fields;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public void setSort(final String sort) {
        this.sort = sort;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public void setOffset(final Integer offset) {
        this.offset = offset;
    }

    @Override
    public Integer getMaxLimit() {
        return maxLimit;
    }

    @Override
    public void setMaxLimit(final Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public void setIdAsString(String idStr) {
        if (idStr != null) {
            try {
                idStr = idStr.trim();
                setId(Long.parseUnsignedLong(idStr));
            } catch (NumberFormatException ex) {
                throw new RestApiException(RestApiException.OPERADOR_ID_NAO_EH_INTEIRO_MAIOR_QUE_ZERO);
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
        this.maxId = maxId;
    }

    @Override
    public RestApiDataFormat getDataFormat() {
        return this.dataFormat;
    }

    @Override
    public void setDataFormat(RestApiDataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    @Override
    public void setDataFormatAsString(final String dataFormatStr) {
        if (dataFormatStr != null) {
            setDataFormat(RestApiDataFormat.strToEnum(dataFormatStr));
        } else {
            this.dataFormat = null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.filter);
        hash = 17 * hash + Objects.hashCode(this.fields);
        hash = 17 * hash + Objects.hashCode(this.sort);
        hash = 17 * hash + Objects.hashCode(this.limit);
        hash = 17 * hash + Objects.hashCode(this.offset);
        hash = 17 * hash + Objects.hashCode(this.id);
        hash = 17 * hash + Objects.hashCode(this.dataFormat);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RestApiRequest other = (RestApiRequest) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        if (!Objects.equals(this.sort, other.sort)) {
            return false;
        }
        if (!Objects.equals(this.limit, other.limit)) {
            return false;
        }
        if (!Objects.equals(this.offset, other.offset)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.dataFormat != other.dataFormat) {
            return false;
        }
        return true;
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
        return "RestApiRequest{" + "filter=" + filter + ", fields=" + fields + ", sort=" + sort + ", limit=" + limit + ", offset=" + offset + ", id=" + id + ", dataFormat=" + dataFormat + '}';
    }

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public HashMap<String, Object> getParameters() {
        return paramters;
    }

    @Override
    public void setParameter(String parameterName, Object value) {
        paramters.put(parameterName, value);
    }

    @Override
    public Map<String, Object> getPayloadAsMap() {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        } else {
            throw new RestApiException(RestApiException.INVALID_PAYLOAD_JSON);
        }
    }

    @Override
    public void setPayload(final Object payload) {
        this.payload = payload;
    }

    @Override
    public Object getObject(final Class objectClass, final Class apiProviderClass) {
        try {
            Object result = objectClass.newInstance();
            IRestApiProvider apiProvider = RestApiProviderFactory.createInstance(apiProviderClass);
            result = RestUtils.setValuesFromMap(result, getPayloadAsMap(), null, apiProvider);
            return result;
        } catch (Exception ex) {
            Logger.getLogger(RestApiRequest.class.getName()).log(Level.SEVERE, null, ex);
            throw new RestApiException(RestApiException.INVALID_PAYLOAD_JSON);
        }
    }

    @Override
    public Object getObject() {
        return this.object;
    }

    @Override
    public void setObject(Object obj) {
        this.object = obj;
    }

    @Override
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public String getAuthorization() {
        return authorization;
    }

}
