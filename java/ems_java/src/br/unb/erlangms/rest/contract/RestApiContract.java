package br.unb.erlangms.rest.contract;

import br.unb.erlangms.rest.cache.IRestApiCachePolicyConfig;
import br.unb.erlangms.rest.cache.RestApiCachePolicyConfig;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequest;
import br.unb.erlangms.rest.request.RestApiRequest;
import br.unb.erlangms.rest.request.RestApiRequestConditionOperator;
import br.unb.erlangms.rest.request.RestApiRequestOperator;
import br.unb.erlangms.rest.schema.IRestApiSchema;
import br.unb.erlangms.rest.schema.RestApiSchema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe de implementação para IRestApiContract responsável
 * por definir o contrato do provider do serviço.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 05/04/2019
 *
 */
public class RestApiContract implements IRestApiContract {
    private static final long serialVersionUID = -3369632657631893106L;
    private static final ArrayList<RestApiRequestConditionOperator> supportFieldOperatorsDefault;
    private static final ArrayList<RestApiRequestOperator> supportApiOperatorsDefault;
    private static final ArrayList<RestApiDataFormat> supportDataFormatDefault;
    private static final ArrayList<RestApiVerb> supportApiVerbsDefault;

    private final IRestApiProvider apiProvider;

    // Se true, o provider aceita operadores filter com and/or
    // Em algumas ocasiões, setSupportAndOrCondition vai sempre ser false, por exemplo
    // quando usa viewSql e o sql contém parâmetros chumbado pelo desenvolvedor
    private boolean supportAndOrCondition;

    // Quais operadores de atributo será permitido para um provider específico
    private final List<RestApiRequestConditionOperator> supportFieldOperators;

    // Quais operações a api vai aceitar em um provider específico
    private final List<RestApiRequestOperator> supportApiOperators;

    // Quais formatos de dados será permitido para um provider específico
    private final List<RestApiDataFormat> supportDataFormat;

    // Quais operações a api vai requerer em um provider específico
    private final List<RestApiRequestOperator> requiredApiOperators;

    // Quais os verbos suportados
    private final List<RestApiVerb> supportApiVerbs;

    // Se true, vai usar a capacidade de named query da JPA
    private boolean supportNamedQuery;

    // Usado para impor defaults para todos os operadores da requisição
    private final IRestApiRequest requestDefault;

    // O esquema descreve os atributos da entidade
    private final IRestApiSchema schema;

    private final IRestApiCachePolicyConfig cachePolicyConfig;

    private boolean authorizationRequired;

    static {
        supportFieldOperatorsDefault = new ArrayList(Arrays.asList(RestApiRequestConditionOperator.Equal,
                                                                   RestApiRequestConditionOperator.Contains,
                                                                   RestApiRequestConditionOperator.IContains,
                                                                   RestApiRequestConditionOperator.Like,
                                                                   RestApiRequestConditionOperator.ILike,
                                                                   RestApiRequestConditionOperator.NotContains,
                                                                   RestApiRequestConditionOperator.INotContains,
                                                                   RestApiRequestConditionOperator.NotLike,
                                                                   RestApiRequestConditionOperator.INotLike,
                                                                   RestApiRequestConditionOperator.GrantThen,
                                                                   RestApiRequestConditionOperator.GrantThenEgual,
                                                                   RestApiRequestConditionOperator.LessThen,
                                                                   RestApiRequestConditionOperator.LessThenEqual,
                                                                   RestApiRequestConditionOperator.NotEqual,
                                                                   RestApiRequestConditionOperator.IsNull));

        supportApiOperatorsDefault = new ArrayList(Arrays.asList(RestApiRequestOperator.Filter,
                                                                 RestApiRequestOperator.Fields,
                                                                 RestApiRequestOperator.Sort,
                                                                 RestApiRequestOperator.Limit,
                                                                 RestApiRequestOperator.Offset,
                                                                 RestApiRequestOperator.Id,
                                                                 RestApiRequestOperator.Format));

        supportDataFormatDefault = new ArrayList(Arrays.asList(RestApiDataFormat.RAW,
                                                               RestApiDataFormat.VO,
                                                               RestApiDataFormat.ENTITY));

        supportApiVerbsDefault = new ArrayList(Arrays.asList(RestApiVerb.GET,
                                                             RestApiVerb.PUT,
                                                             RestApiVerb.POST,
                                                             RestApiVerb.DELETE,
                                                             RestApiVerb.HEAD));

    }

    public RestApiContract(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        this.schema = new RestApiSchema(this);
        this.supportAndOrCondition = true;
        this.supportFieldOperators = (List<RestApiRequestConditionOperator>) supportFieldOperatorsDefault.clone();
        this.supportApiOperators = (List<RestApiRequestOperator>) supportApiOperatorsDefault.clone();
        this.supportDataFormat = (List<RestApiDataFormat>) supportDataFormatDefault.clone();
        this.supportApiVerbs = (List<RestApiVerb>) supportApiVerbsDefault.clone();
        this.requestDefault = new RestApiRequest();
        this.requiredApiOperators = new ArrayList<>();
        this.supportNamedQuery = true;
        this.cachePolicyConfig = new RestApiCachePolicyConfig();
        this.authorizationRequired = true;
    }

    @Override
    public void setSupportAndOrCondition(boolean value) {
        this.supportAndOrCondition = value;
    }

    @Override
    public boolean isSupportAndOrCondition() {
        return this.supportAndOrCondition;
    }

    @Override
    public boolean isSupportFieldOperator(RestApiRequestConditionOperator fieldOperator) {
        return supportFieldOperators.contains(fieldOperator);
    }

    @Override
    public boolean isSupportApiOperator(RestApiRequestOperator apiOperator) {
        return supportApiOperators.contains(apiOperator);
    }

    @Override
    public List<RestApiRequestConditionOperator> getSupportConditionOperators() {
        return supportFieldOperators;
    }

    @Override
    public List<RestApiRequestOperator> getSupportApiOperators() {
        return supportApiOperators;
    }

    @Override
    public List<RestApiDataFormat> getSupportApiDataFormat() {
        return supportDataFormat;
    }

    @Override
    public boolean isSupportApiDataFormat(RestApiDataFormat dataFormat) {
        return supportDataFormat.contains(dataFormat);
    }

    @Override
    public List<RestApiRequestOperator> getRequiredApiOperators() {
        return requiredApiOperators;
    }

    @Override
    public boolean isRequiredApiOperator(RestApiRequestOperator apiOperator) {
        return requiredApiOperators.contains(apiOperator);
    }

    @Override
    public IRestApiRequest getRequestDefault() {
        return requestDefault;
    }

    @Override
    public boolean isSupportNamedQuery() {
        return supportNamedQuery;
    }

    @Override
    public void setSupportNamedQuery(boolean supportNamedQuery) {
        this.supportNamedQuery = supportNamedQuery;
    }

    @Override
    public IRestApiCachePolicyConfig getCachePolicyConfig() {
        return cachePolicyConfig;
    }

    @Override
    public void checkSupportApiOperator(final RestApiRequestOperator operator) {
        if (!isSupportApiOperator(operator)) {
            throw new RestApiException("Operador " + RestApiRequestOperator.enumToOperatorToken(operator) + " não permitido para o webservice solicitado.");
        }
        if (operator == RestApiRequestOperator.Id && apiProvider.getViewSql() != null) {
            throw new RestApiException("Operador " + RestApiRequestOperator.enumToOperatorToken(operator) + " não permitido para o webservice solicitado.");
        }
    }

    @Override
    public void checkSupportFieldOperator(final RestApiRequestConditionOperator fieldOperator) {
        if (!isSupportFieldOperator(fieldOperator)) {
            throw new RestApiException("Operador de atributo " + RestApiRequestConditionOperator.enumToFieldOperatorToken(fieldOperator) + " não permitido para o webservice solicitado.");
        }
    }

    @Override
    public void checkSupportDataFormat(final RestApiDataFormat dataFormat) {
        if (!isSupportApiDataFormat(dataFormat)) {
            throw new RestApiException("Formato de dados " + RestApiDataFormat.enumToStr(dataFormat) + " não permitido para o webservice solicitado.");
        }
    }

    @Override
    public IRestApiSchema getSchema() {
        return schema;
    }

    @Override
    public IRestApiProvider getApiProvider() {
        return apiProvider;
    }

    @Override
    public List<RestApiVerb> getSupportApiVerbs() {
        return supportApiVerbs;
    }

    @Override
    public boolean isSupportApiVerb(RestApiVerb verb) {
        return supportApiVerbs.contains(verb);
    }

    @Override
    public void checkSupportApiVerb(RestApiVerb apiVerb) {
        if (!isSupportApiVerb(apiVerb)) {
            throw new RestApiException(RestApiException.WS_NAO_SUPORTA_VERBO);
        }
    }

    @Override
    public void setAuthorizationRequired(boolean required) {
        this.authorizationRequired = true;
    }

    @Override
    public boolean isAuthorizationRequired() {
        return this.authorizationRequired;
    }

}
