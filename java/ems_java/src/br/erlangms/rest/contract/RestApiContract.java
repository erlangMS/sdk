/*
 * 
 */
package br.erlangms.rest.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.cache.IRestApiCachePolicyConfig;
import br.erlangms.rest.cache.RestApiCachePolicyConfig;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.request.RestApiRequest;
import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.request.RestApiRequestOperator;
import br.erlangms.rest.schema.IRestApiSchema;
import br.erlangms.rest.schema.RestApiSchema;

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
	private static final long serialVersionUID = 751914765003155406L;
	private static final ArrayList<RestApiRequestConditionOperator> supportFieldOperatorsDefault;
    private static final ArrayList<RestApiRequestOperator> supportApiOperatorsDefault;
    private static final ArrayList<RestApiDataFormat> supportDataFormatDefault;

    private IRestApiProvider apiProvider;

    // Se true, o provider aceita operadores filter com and/or
    // Em algumas ocasiÃµes, setSupportAndOrCondition vai sempre ser false, por exemplo
    // quando usa viewSql e o sql contÃ©m parâmetros chumbado pelo desenvolvedor
    private boolean supportAndOrCondition;

    // Quais operadores de atributo será permitido para um provider específico
    private final List<RestApiRequestConditionOperator> supportFieldOperators;

    // Quais operaÃ§Ãµes a api vai aceitar em um provider específico
    private final List<RestApiRequestOperator> supportApiOperators;

    // Quais formatos de dados será permitido para um provider específico
    private final List<RestApiDataFormat> supportDataFormat;

    // Quais operaÃ§Ãµes a api vai requerer em um provider específico
    private final List<RestApiRequestOperator> requiredApiOperators;

    // Se true, vai usar a capacidade de named query da JPA
    private boolean supportNamedQuery;

    // Usado para impor defaults para todos os operadores da requisição
    private IRestApiRequest requestDefault;

    // O esquema descreve os atributos da entidade
    private final IRestApiSchema schema;

    private final IRestApiCachePolicyConfig cachePolicyConfig;

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

    }

    public RestApiContract(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        this.schema = new RestApiSchema(this);
        this.supportAndOrCondition = true;
        this.supportFieldOperators = (List<RestApiRequestConditionOperator>) supportFieldOperatorsDefault.clone();
        this.supportApiOperators = (List<RestApiRequestOperator>) supportApiOperatorsDefault.clone();
        this.supportDataFormat = (List<RestApiDataFormat>) supportDataFormatDefault.clone();
        this.requestDefault = RestApiRequest.createDefaultRequest(this.apiProvider);
        this.requiredApiOperators = new ArrayList<>();
        this.supportNamedQuery = true;
        this.cachePolicyConfig = new RestApiCachePolicyConfig();
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
    public List<RestApiDataFormat> getSupportDataFormat() {
        return supportDataFormat;
    }

    @Override
    public boolean isSupportDataFormat(RestApiDataFormat dataFormat) {
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
    public void setRequestDefault(IRestApiRequest requestDefault) {
        this.requestDefault = requestDefault;
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
            throw new RestApiConstraintException("Operador " + RestApiRequestOperator.enumToOperatorToken(operator) + " não permitido para o webservice solicitado.");
        }
        if (operator == RestApiRequestOperator.Id && apiProvider.getViewSql() != null) {
            throw new RestApiConstraintException("Operador " + RestApiRequestOperator.enumToOperatorToken(operator) + " não permitido para o webservice solicitado.");
        }
    }

    @Override
    public void checkSupportFieldOperator(final RestApiRequestConditionOperator fieldOperator) {
        if (!isSupportFieldOperator(fieldOperator)) {
            throw new RestApiConstraintException("Operador de atributo " + RestApiRequestConditionOperator.enumToFieldOperatorToken(fieldOperator) + " não permitido para o webservice solicitado.");
        }
    }

    @Override
    public void checkSupportDataFormat(final RestApiDataFormat dataFormat) {
        if (!isSupportDataFormat(dataFormat)) {
            throw new RestApiConstraintException("Formato de dados " + RestApiDataFormat.enumToStr(dataFormat) + " não permitido para o webservice solicitado.");
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

    public void setApiProvider(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }


}
