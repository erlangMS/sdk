package br.unb.erlangms.rest.provider;

import br.unb.erlangms.rest.IRestApiManager;
import br.unb.erlangms.rest.contract.IRestApiContract;
import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.query.IRestQueryGenerator;
import br.unb.erlangms.rest.query.RestJpaFilterGeneratorDefineIfParameterIsFromViewSql;
import br.unb.erlangms.rest.query.RestJpaFilterGeneratorEmitCodeCallback;
import br.unb.erlangms.rest.query.RestJpaParameterQueryCallback;
import br.unb.erlangms.rest.query.RestJpaQueryGenerator;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import br.unb.erlangms.rest.request.RestApiRequestOperator;
import br.unb.erlangms.rest.schema.RestField;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

/**
 * Classe de implementação do IRestApiProvider
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 13/03/2019
 *
 */
public abstract class RestApiProvider implements IRestApiProvider {
    private IRestApiManager apiManager;
    private Class voClass;
    private Class entityClass;
    private String viewSql;
    private RestFieldValueSerializeCallback fieldValueSerializeAsStringCallback;
    private RestFieldValueSerializeCallback fieldValueSerializeAsIntegerCallback;
    private RestFieldValueSerializeCallback fieldValueSerializeAsDoubleCallback;
    private RestFieldValueSerializeCallback fieldValueSerializeAsLongCallback;
    private RestFieldValueSerializeCallback fieldValueSerializeAsDateCallback;
    private RestFieldValueSerializeCallback fieldValueSerializeAsBooleanCallback;
    private RestJpaParameterQueryCallback parameterQueryTransform;
    private RestJpaFilterGeneratorDefineIfParameterIsFromViewSql defineIfParameterIsFromViewSql;
    private RestJpaFilterGeneratorEmitCodeCallback emitCodeFilterConditionCallback;
    private IRestApiContract contract;

    public RestApiProvider() {
        this.apiManager = null;
        this.voClass = null;
        this.entityClass = null;
        this.viewSql = null;
        this.fieldValueSerializeAsStringCallback = null;
        this.fieldValueSerializeAsIntegerCallback = null;
        this.fieldValueSerializeAsDoubleCallback = null;
        this.fieldValueSerializeAsLongCallback = null;
        this.fieldValueSerializeAsDateCallback = null;
        this.fieldValueSerializeAsBooleanCallback = null;
        this.parameterQueryTransform = null;
        this.defineIfParameterIsFromViewSql = null;
        this.emitCodeFilterConditionCallback = null;
    }

    protected void setVoClass(Class aClass) {
        this.voClass = aClass;
    }

    @Override
    public Class getVoClass() {
        return voClass;
    }

    @Override
    public Class getEntityClass() {
        return entityClass;
    }

    protected void setEntityClass(Class aClass) {
        this.entityClass = aClass;
    }

    public RestFieldValueSerializeCallback getFieldJsonTransform() {
        return fieldValueSerializeAsStringCallback;
    }

    @Override
    public void setFieldValueSerializeAsStringCallback(RestFieldValueSerializeCallback callback) {
        this.fieldValueSerializeAsStringCallback = callback;
    }

    @Override
    public RestFieldValueSerializeCallback getFieldValueSerializeAsStringCallback() {
        return fieldValueSerializeAsStringCallback;
    }

    @Override
    public RestFieldValueSerializeCallback getFieldValueSerializeAsIntegerCallback() {
        return fieldValueSerializeAsIntegerCallback;
    }

    @Override
    public void setFieldValueSerializeAsIntegerCallback(RestFieldValueSerializeCallback fieldValueSerializeAsIntegerCallback) {
        this.fieldValueSerializeAsIntegerCallback = fieldValueSerializeAsIntegerCallback;
    }

    @Override
    public RestFieldValueSerializeCallback getFieldValueSerializeAsDoubleCallback() {
        return fieldValueSerializeAsDoubleCallback;
    }

    @Override
    public void setFieldValueSerializeAsDoubleCallback(RestFieldValueSerializeCallback fieldValueSerializeAsDoubleCallback) {
        this.fieldValueSerializeAsDoubleCallback = fieldValueSerializeAsDoubleCallback;
    }

    @Override
    public RestFieldValueSerializeCallback getFieldValueSerializeAsLongCallback() {
        return fieldValueSerializeAsLongCallback;
    }

    @Override
    public void setFieldValueSerializeAsLongCallback(RestFieldValueSerializeCallback fieldValueSerializeAsLongCallback) {
        this.fieldValueSerializeAsLongCallback = fieldValueSerializeAsLongCallback;
    }

    @Override
    public RestFieldValueSerializeCallback getFieldValueSerializeAsDateCallback() {
        return fieldValueSerializeAsDateCallback;
    }

    @Override
    public void setFieldValueSerializeAsDateCallback(RestFieldValueSerializeCallback fieldValueSerializeAsDateCallback) {
        this.fieldValueSerializeAsDateCallback = fieldValueSerializeAsDateCallback;
    }

    @Override
    public RestFieldValueSerializeCallback getFieldValueSerializeAsBooleanCallback() {
        return fieldValueSerializeAsBooleanCallback;
    }

    @Override
    public void setFieldValueSerializeAsBooleanCallback(RestFieldValueSerializeCallback fieldValueSerializeAsBooleanCallback) {
        this.fieldValueSerializeAsBooleanCallback = fieldValueSerializeAsBooleanCallback;
    }

    @Override
    public String getViewSql() {
        return viewSql;
    }

    protected void setViewSql(final String viewSql) {
        this.viewSql = viewSql;
    }

    /**
     * Cria um EntityGraph dinâmico para otimizar as consultas JPA
     *
     * Obs: Não usado ainda na UFSM pois a versão do hibernate parece que  Não suporta
     *
     * @author Everton de Vargas Agilar
     * @param entityManager
     * @return
     */
    @Override
    public EntityGraph getEntityGraph(final EntityManager entityManager) {
        EntityGraph<?> result = entityManager.createEntityGraph(getEntityClass());
        for (RestField field : getContract().getSchema().getFieldsList()) {
            if (field.isAttributeObject()) {
                result.addSubgraph(field.getAttrBaseRelationName());
            }
        }
        return result;
    }

    @Override
    public RestJpaParameterQueryCallback getParameterQueryCallback() {
        return parameterQueryTransform;
    }

    @Override
    public void setParameterQueryCallback(RestJpaParameterQueryCallback parameterQueryTransform) {
        this.parameterQueryTransform = parameterQueryTransform;
    }

    @Override
    public RestJpaFilterGeneratorDefineIfParameterIsFromViewSql getDefineIfParameterIsFromViewSql() {
        return defineIfParameterIsFromViewSql;
    }

    @Override
    public void setDefineIfParameterIsFromViewSql(RestJpaFilterGeneratorDefineIfParameterIsFromViewSql defineIfParameterIsFromViewSql) {
        this.defineIfParameterIsFromViewSql = defineIfParameterIsFromViewSql;
    }

    @Override
    public RestJpaFilterGeneratorEmitCodeCallback getEmitCodeFilterConditionCallback() {
        return emitCodeFilterConditionCallback;
    }

    @Override
    public void setEmitCodeFilterConditionCallback(RestJpaFilterGeneratorEmitCodeCallback callback) {
        this.emitCodeFilterConditionCallback = callback;
    }

    @Override
    public void validateRequestWithContract(final IRestApiRequestInternal request) {
        if (request == null) {
            throw new RestApiException(RestApiException.REQUEST_OBRIGATORIO_PARA_CRIAR_QUERY);
        }

        if (!request.isParsed() && getContract() != null) {

            // Infelizmente, se um callback defineIfParameterIsFromViewSql for definido
            //  Não será possível usar condições and ou or
            // Motivo: quando uma viewSql possui Parâmetros fixos, o gerador de query  Não consegue
            // criar adequadamente o filtro
            if (defineIfParameterIsFromViewSql != null && contract != null && contract.isSupportAndOrCondition()) {
                contract.setSupportAndOrCondition(false);
            }

            // Se tiver viewSql,  Não suporta o operador id
            if (this.getViewSql() != null) {
                contract.getSupportApiOperators().remove(RestApiRequestOperator.Id);
            }

            // Se o request tem o format verifica se o provider suporta
            if (request.getApiDataFormat() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Format);
            }

            // Se o provider requer o operador format, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Format) && request.getApiDataFormat() == null) {
                throw new RestApiException(RestApiException.OPERADOR_FORMAT_OBRIGATORIO);
            }

            // Se o request tem o operador id verifica se o provider suporta
            if (request.getId() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Id);
            }

            // Se o provider requer o operador id, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Id) && request.getId() == null) {
                throw new RestApiException(RestApiException.OPERADOR_ID_OBRIGATORIO);
            }

            // Se o request tem o operador filter verifica se o provider suporta
            if (request.getFilterAST() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Filter);
            }

            // Se o provider requer o operador filter, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Filter) && request.getFilterAST() == null) {
                throw new RestApiException(RestApiException.OPERADOR_FILTER_OBRIGATORIO);
            }

            // Se o request tem o operador limit verifica se o provider suporta
            if (request.getLimit() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Limit);
            }

            // Se o provider requer o operador limit, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Limit) && request.getLimit() == null) {
                throw new RestApiException(RestApiException.OPERADOR_LIMIT_OBRIGATORIO);
            }

            // Se o request tem o operador offset verifica se o provider suporta
            if (request.getOffset() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Offset);
            }

            // Se o provider requer o operador offset, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Offset) && request.getOffset() == null) {
                throw new RestApiException(RestApiException.OPERADOR_OFFSET_OBRIGATORIO);
            }

            // Se o request tem o operador fields verifica se o provider suporta
            if (!request.getFieldsList().isEmpty()) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Fields);
            }

            // Se o provider requer o operador fields, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Fields) && request.getFieldsList().isEmpty()) {
                throw new RestApiException(RestApiException.OPERADOR_FIELDS_OBRIGATORIO);
            }

            // Se o request tem o operador sort verifica se o provider suporta
            if (!request.getSortList().isEmpty()) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Sort);
            }

            // Se o provider requer o operador sort, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Sort) && request.getSortList().isEmpty()) {
                throw new RestApiException(RestApiException.OPERADOR_SORT_OBRIGATORIO);
            }

            if (request.getApiDataFormat() == RestApiDataFormat.ENTITY && getEntityClass() == null){
                throw new RestApiException(RestApiException.DATA_FORMAT_ENTITY_REQUER_ENTITY_CLASS);
            }

            // Valida filtros obrigatórios
            for (RestField field : contract.getSchema().getRequiredFieldsList()){
                if (!request.findConditionByVoFieldName(field).isPresent()){
                    throw new RestApiException(RestApiException.ATRIBUTO_OBRIGATORIO_NO_FILTRO, field.getVoFieldName());
                }
            }

            // Valida se o verbo é permitido
            contract.checkSupportApiVerb(request.getApiVerb());

        }
    }

    @Override
    public IRestApiManager getApiManager() {
        return apiManager;
    }

    @Override
    public void setApiManager(IRestApiManager apiManager
    ) {
        this.apiManager = apiManager;
    }

    @Override
    public IRestApiContract getContract() {
        if (contract == null) {
            contract = createContract();
            afterCreateContract();
        }
        return contract;
    }

    @Override
    public IRestApiContract __getContract() {
        return contract;
    }

    @Override
    public IRestQueryGenerator createQueryGenerator() {
        return new RestJpaQueryGenerator();
    }

    protected void afterCreateContract() {

    }

}
