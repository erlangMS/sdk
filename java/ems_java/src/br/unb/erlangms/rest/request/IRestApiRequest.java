package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.contract.RestApiDataFormat;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evertonagilar
 */
public interface IRestApiRequest extends Serializable {
    // Operadores padrão da API
    public String getFields();
    public String getFilter();
    public Long getId();
    public Integer getLimit();
    public Long getMaxId();
    public Integer getMaxLimit();
    public Integer getOffset();
    public String getSort();
    public void setFields(final String fields);
    public void setFilter(String filter);
    public void setId(final Long id);
    public void setIdAsString(String idStr);
    public void setLimit(Integer limit);
    public void setMaxId(final Long maxId);
    public void setMaxLimit(Integer maxLimit);
    public void setOffset(Integer offset);
    public void setSort(final String sort);

    // Permite selecionar a engine de serialização utilizar com base no formato de dados
    public RestApiDataFormat getDataFormat();
    public void setDataFormat(RestApiDataFormat dataFormat);
    public void setDataFormatAsString(final String dataFormatStr);

    // Flags permitem fazer pequenos ajustes na execução da requisição
    public IRestApiRequestFlags getFlags();
    public void setFlagsAsString(String flags);

    // O operador filter pode ser parametrizado e os parâmetros são setados com esses métodos
    public HashMap<String, Object> getParamters();
    public void setParameter(String parameterName, Object value);

    // Utilizado em requisições PUT
    public Map<String, Object> getPayloadAsMap();
    public void setPayload(final Object payload);

    public Object getObject();
    public void setObject(Object obj);

    // Permite setar o token OAuth2
    public void setAuthorization(final String authorization);
    public String getAuthorization();
}
