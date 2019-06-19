package br.unb.erlangms.rest.contract;

import br.unb.erlangms.rest.cache.IRestApiCachePolicyConfig;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequest;
import br.unb.erlangms.rest.request.RestApiRequestConditionOperator;
import br.unb.erlangms.rest.request.RestApiRequestOperator;
import br.unb.erlangms.rest.schema.IRestApiSchema;
import java.io.Serializable;
import java.util.List;

/**
 * Interface responsável por definir o contrato de um RestApiProvider.
 * O contrato define por exemplo, se o provider aceita operadores filter com and/and ou os
 * dados podem ser ordenados com o operador sort e o schema de dados retornado.
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 18/04/2019
 *
 */
public interface IRestApiContract extends Serializable {
    public void checkSupportApiOperator(RestApiRequestOperator operator);
    public void checkSupportDataFormat(RestApiDataFormat dataFormat);
    public void checkSupportFieldOperator(RestApiRequestConditionOperator fieldOperator);
    public void checkSupportApiVerb(RestApiVerb apiVerb);
    public IRestApiRequest getRequestDefault();
    public List<RestApiRequestOperator> getRequiredApiOperators();
    public List<RestApiRequestOperator> getSupportApiOperators();
    public List<RestApiDataFormat> getSupportApiDataFormat();
    public List<RestApiRequestConditionOperator> getSupportConditionOperators();
    public List<RestApiVerb> getSupportApiVerbs();
    public boolean isRequiredApiOperator(RestApiRequestOperator apiOperator);
    public boolean isSupportAndOrCondition();
    public boolean isSupportApiOperator(RestApiRequestOperator apiOperator);
    public boolean isSupportApiDataFormat(RestApiDataFormat dataFormat);
    public boolean isSupportFieldOperator(RestApiRequestConditionOperator fieldOperator);
    public boolean isSupportNamedQuery();
    public boolean isSupportApiVerb(RestApiVerb verb);
    public void setSupportAndOrCondition(boolean value);
    public void setSupportNamedQuery(boolean supportNamedQuery);
    public void setAuthorizationRequired(boolean required);
    public boolean isAuthorizationRequired();
    public IRestApiSchema getSchema();
    public IRestApiProvider getApiProvider();
    public IRestApiCachePolicyConfig getCachePolicyConfig();
}
