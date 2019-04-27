/*
 * 
 */
package br.erlangms.rest.contract;

import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.cache.IRestApiCachePolicyConfig;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.request.RestApiRequestOperator;
import br.erlangms.rest.schema.IRestApiSchema;
import java.io.Serializable;
import java.util.List;

/**
 * Interface responsável por definir o contrato de um RestApiProvider.
 * 
 * O contrato define por exemplo, se o provider aceita operadores filter com and/and ou os
 * dados podem ser ordenados com o operador sort e o schema de dados retornado.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 18/04/2019
 *
 */
public interface IRestApiContract extends Serializable {
    public void checkSupportApiOperator(RestApiRequestOperator operator);
    public void checkSupportDataFormat(RestApiDataFormat dataFormat);
    public void checkSupportFieldOperator(RestApiRequestConditionOperator fieldOperator);
    public IRestApiRequest getRequestDefault();
    public List<RestApiRequestOperator> getRequiredApiOperators();
    public List<RestApiRequestOperator> getSupportApiOperators();
    public List<RestApiDataFormat> getSupportDataFormat();
    public List<RestApiRequestConditionOperator> getSupportConditionOperators();
    public boolean isRequiredApiOperator(RestApiRequestOperator apiOperator);
    public boolean isSupportAndOrCondition();
    public boolean isSupportApiOperator(RestApiRequestOperator apiOperator);
    public boolean isSupportDataFormat(RestApiDataFormat dataFormat);
    public boolean isSupportFieldOperator(RestApiRequestConditionOperator fieldOperator);
    public boolean isSupportNamedQuery();
    public void setRequestDefault(IRestApiRequest requestDefault);
    public void setSupportAndOrCondition(boolean value);
    public void setSupportNamedQuery(boolean supportNamedQuery);
    public IRestApiSchema getSchema();
    public IRestApiProvider getApiProvider();
    public IRestApiCachePolicyConfig getCachePolicyConfig();
}
