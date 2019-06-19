package br.unb.erlangms.rest.provider;

import br.unb.erlangms.rest.IRestApiManager;
import br.unb.erlangms.rest.contract.IRestApiContract;
import br.unb.erlangms.rest.query.IRestQueryGenerator;
import br.unb.erlangms.rest.query.RestJpaFilterGeneratorDefineIfParameterIsFromViewSql;
import br.unb.erlangms.rest.query.RestJpaFilterGeneratorEmitCodeCallback;
import br.unb.erlangms.rest.query.RestJpaParameterQueryCallback;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import java.io.Serializable;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

/**
 * Interface responsável por representar o provedor de conteúdo da Api RESTful
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
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
    public RestJpaFilterGeneratorEmitCodeCallback getEmitCodeFilterConditionCallback();
    public void setEmitCodeFilterConditionCallback(final RestJpaFilterGeneratorEmitCodeCallback callback);
    public IRestApiContract createContract();
    public IRestApiContract getContract();
    public void validateRequestWithContract(IRestApiRequestInternal request);
    public IRestQueryGenerator createQueryGenerator();
    public IRestApiManager getApiManager();
    public void setApiManager(IRestApiManager apiManager);
    public void setFieldValueSerializeAsStringCallback(RestFieldValueSerializeCallback callback);
    public RestFieldValueSerializeCallback getFieldValueSerializeAsStringCallback();
    public void setFieldValueSerializeAsIntegerCallback(RestFieldValueSerializeCallback callback);
    public RestFieldValueSerializeCallback getFieldValueSerializeAsIntegerCallback();
    public void setFieldValueSerializeAsDoubleCallback(RestFieldValueSerializeCallback callback);
    public RestFieldValueSerializeCallback getFieldValueSerializeAsDoubleCallback();
    public void setFieldValueSerializeAsLongCallback(RestFieldValueSerializeCallback callback);
    public RestFieldValueSerializeCallback getFieldValueSerializeAsLongCallback();
    public void setFieldValueSerializeAsDateCallback(RestFieldValueSerializeCallback callback);
    public RestFieldValueSerializeCallback getFieldValueSerializeAsDateCallback();
    public void setFieldValueSerializeAsBooleanCallback(RestFieldValueSerializeCallback callback);
    public RestFieldValueSerializeCallback getFieldValueSerializeAsBooleanCallback();
    public IRestApiContract __getContract();

}
