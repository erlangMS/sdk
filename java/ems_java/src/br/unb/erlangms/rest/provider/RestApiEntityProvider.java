package br.unb.erlangms.rest.provider;

import br.unb.erlangms.rest.query.IRestQueryGenerator;
import br.unb.erlangms.rest.query.RestJpaQueryGenerator;

/**
 *
 * @author evertonagilar
 */
public abstract class RestApiEntityProvider extends RestApiProvider implements IRestApiProvider {

    public RestApiEntityProvider() {
        super();
    }

    @Override
    public IRestQueryGenerator createQueryGenerator(){
        return new RestJpaQueryGenerator();
    }

}
