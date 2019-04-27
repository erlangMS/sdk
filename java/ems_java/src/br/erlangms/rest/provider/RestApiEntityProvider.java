/*
 * 
 */
package br.erlangms.rest.provider;

import br.erlangms.rest.query.IRestQueryGenerator;
import br.erlangms.rest.query.RestJpaQueryGenerator;

/**
 *
 * @author evertonagilar
 */
public abstract class RestApiEntityProvider extends RestApiProvider implements IRestApiProvider {

    public RestApiEntityProvider() {
        super();
    }

    public RestApiEntityProvider(Class entityClass) {
        super(entityClass);
    }

    public RestApiEntityProvider(Class jsonClass, Class entityClass) {
        super(jsonClass, entityClass);
    }

    @Override
    public IRestQueryGenerator createQueryGenerator(){
        return new RestJpaQueryGenerator();
    }


}
