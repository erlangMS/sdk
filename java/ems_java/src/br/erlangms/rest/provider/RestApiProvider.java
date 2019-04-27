/*
 * 
 */
package br.erlangms.rest.provider;

import java.util.ArrayList;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

import br.erlangms.rest.IRestApiManager;
import br.erlangms.rest.contract.IRestApiContract;
import br.erlangms.rest.contract.RestApiContract;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.query.IRestQueryGenerator;
import br.erlangms.rest.query.RestJpaFilterGeneratorDefineIfParameterIsFromViewSql;
import br.erlangms.rest.query.RestJpaFilterGeneratorEmitCodeTransform;
import br.erlangms.rest.query.RestJpaParameterQueryCallback;
import br.erlangms.rest.query.RestJpaQueryGenerator;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.request.RestApiRequestOperator;
import br.erlangms.rest.schema.RestField;

/**
 * Classe de implementação do IRestApiProvider
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 13/03/2019
 *
 */
public abstract class RestApiProvider implements IRestApiProvider {
	private static final long serialVersionUID = -2447171474984018997L;
	private IRestApiManager apiManager;
    private Class voClass;
    private Class entityClass;
    private String viewSql;
    private RestFieldValueSerializeTransform fieldVoTransform;
    private RestJpaParameterQueryCallback parameterQueryTransform;
    private RestJpaFilterGeneratorDefineIfParameterIsFromViewSql defineIfParameterIsFromViewSql;
    private RestJpaFilterGeneratorEmitCodeTransform emitCodeTransform;
    private IRestApiContract contract;

    public RestApiProvider() {
        this.apiManager = null;
        this.voClass = null;
        this.entityClass = null;
        this.viewSql = null;
        this.fieldVoTransform = null;
        this.parameterQueryTransform = null;
        this.defineIfParameterIsFromViewSql = null;
        this.emitCodeTransform = null;
    }

    public RestApiProvider(Class entityClass) {
        this.apiManager = null;
        this.voClass = null;
        this.entityClass = entityClass;
        this.viewSql = null;
        this.fieldVoTransform = null;
        this.parameterQueryTransform = null;
        this.defineIfParameterIsFromViewSql = null;
        this.emitCodeTransform = null;
    }

    public RestApiProvider(Class jsonClass, Class entityClass) {
        this.apiManager = null;
        this.voClass = jsonClass;
        this.entityClass = entityClass;
        this.viewSql = null;
        this.fieldVoTransform = null;
        this.parameterQueryTransform = null;
        this.defineIfParameterIsFromViewSql = null;
        this.emitCodeTransform = null;
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

    public RestFieldValueSerializeTransform getFieldJsonTransform() {
        return fieldVoTransform;
    }

    @Override
    public void setFieldValueSerializeTransform(RestFieldValueSerializeTransform fieldTransform) {
        this.fieldVoTransform = fieldTransform;
    }

    @Override
    public RestFieldValueSerializeTransform getFieldVoTransform() {
        return fieldVoTransform;
    }

    @Override
    public String getViewSql() {
        return viewSql;
    }

    protected void setViewSql(final String viewSql) {
        this.viewSql = viewSql;
    }

    /**
     * Cria um EntityGraph para otimizar as consultas JPA
     *
     * Obs: Ainda não utilizado
     *
     * @author Everton de Vargas Agilar
     * @param entityManager entityManager JPA
     * @return EntityGraph
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
    public RestJpaFilterGeneratorEmitCodeTransform getEmitCodeTransform() {
        return emitCodeTransform;
    }

    @Override
    public void setEmitCodeTransform(RestJpaFilterGeneratorEmitCodeTransform emitCodeTransform) {
        this.emitCodeTransform = emitCodeTransform;
    }

    @Override
    public void validateRequestWithConstraints(final IRestApiRequest request) {
        if (request == null) {
            throw new RestApiConstraintException(RestApiConstraintException.REQUEST_OBRIGATORIO_PARA_CRIAR_QUERY);
        }

        if (!request.isParsed() && getContract() != null) {

            // Infelizmente, se um callback defineIfParameterIsFromViewSql for definido
            // não será possÃ­vel usar condições and ou or
            // Motivo: quando uma viewSql possui parâmetros fixos, o gerador de query não consegue
            // criar adequadamente o filtro
            if (defineIfParameterIsFromViewSql != null && contract != null && contract.isSupportAndOrCondition()) {
                contract.setSupportAndOrCondition(false);
            }

            // Se tiver viewSql, não suporta o operador id
            if (this.getViewSql() != null) {
                contract.getSupportApiOperators().remove(RestApiRequestOperator.Id);
            }

            // Se o request tem o format verifica se o provider suporta
            if (request.getDataFormat() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Format);
            }

            // Se o provider requer o operador format, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Format) && request.getDataFormat() == null) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_FORMAT_OBRIGATORIO);
            }

            // Se o request tem o operador id verifica se o provider suporta
            if (request.getId() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Id);
            }

            // Se o provider requer o operador id, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Id) && request.getId() == null) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_ID_OBRIGATORIO);
            }

            // Se o request tem o operador filter verifica se o provider suporta
            if (request.getFilterAST() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Filter);
            }

            // Se o provider requer o operador filter, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Filter) && request.getFilterAST() == null) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_FILTER_OBRIGATORIO);
            }

            // Se o request tem o operador limit verifica se o provider suporta
            if (request.getLimit() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Limit);
            }

            // Se o provider requer o operador limit, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Limit) && request.getLimit() == null) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_LIMIT_OBRIGATORIO);
            }

            // Se o request tem o operador offset verifica se o provider suporta
            if (request.getOffset() != null) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Offset);
            }

            // Se o provider requer o operador offset, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Offset) && request.getOffset() == null) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_OFFSET_OBRIGATORIO);
            }

            // Se o request tem o operador fields verifica se o provider suporta
            if (!request.getFieldsList().isEmpty()) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Fields);
            }

            // Se o provider requer o operador fields, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Fields) && request.getFieldsList().isEmpty()) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_FIELDS_OBRIGATORIO);
            }

            // Se o request tem o operador sort verifica se o provider suporta
            if (!request.getSortList().isEmpty()) {
                contract.checkSupportApiOperator(RestApiRequestOperator.Sort);
            }

            // Se o provider requer o operador sort, o request precisa fornecer
            if (contract.isRequiredApiOperator(RestApiRequestOperator.Sort) && request.getSortList().isEmpty()) {
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_SORT_OBRIGATORIO);
            }
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
            ((RestApiContract) contract).setApiProvider(this);

            if (contract.getRequestDefault().isDefault()) {
                contract.getRequestDefault().__setFields(String.join(",", contract.getSchema().getVoFields()));
                contract.getRequestDefault().__setFieldsList(new ArrayList<>(contract.getSchema().getFieldsList()));
            }
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

}
