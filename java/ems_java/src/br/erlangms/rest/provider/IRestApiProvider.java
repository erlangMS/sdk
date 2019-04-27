/*
 * 
 */
package br.erlangms.rest.provider;

import java.io.Serializable;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

import br.erlangms.rest.IRestApiManager;
import br.erlangms.rest.contract.IRestApiContract;
import br.erlangms.rest.query.IRestQueryGenerator;
import br.erlangms.rest.query.RestJpaFilterGeneratorDefineIfParameterIsFromViewSql;
import br.erlangms.rest.query.RestJpaFilterGeneratorEmitCodeTransform;
import br.erlangms.rest.query.RestJpaParameterQueryCallback;
import br.erlangms.rest.request.IRestApiRequest;

/**
 * Interface responsável por representar o provedor de conteúdo da Api RESTful
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 15/03/2019
 *
 */
public interface IRestApiProvider extends Serializable {
    public Class getVoClass();
    public Class getEntityClass();
    public EntityGraph getEntityGraph(final EntityManager entityManager);
    public String getViewSql();
    public RestJpaParameterQueryCallback getParameterQueryCallback();
    public void setParameterQueryCallback(RestJpaParameterQueryCallback callback);
    public RestJpaFilterGeneratorDefineIfParameterIsFromViewSql getDefineIfParameterIsFromViewSql();
    public void setDefineIfParameterIsFromViewSql(final RestJpaFilterGeneratorDefineIfParameterIsFromViewSql defineIfParameterIsFromViewSql);
    public RestJpaFilterGeneratorEmitCodeTransform getEmitCodeTransform();
    public void setEmitCodeTransform(final RestJpaFilterGeneratorEmitCodeTransform emitCodeTransform);
    public IRestApiContract createContract();
    public IRestApiContract getContract();
    public void validateRequestWithConstraints(IRestApiRequest request);
    public IRestQueryGenerator createQueryGenerator();
    public IRestApiManager getApiManager();
    public void setApiManager(IRestApiManager apiManager);
    public void setFieldValueSerializeTransform(RestFieldValueSerializeTransform fieldTransform);
    public RestFieldValueSerializeTransform getFieldVoTransform();
    public IRestApiContract __getContract();
}
