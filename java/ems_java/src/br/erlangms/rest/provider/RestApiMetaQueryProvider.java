/*
 * 
 */
package br.erlangms.rest.provider;

import br.erlangms.rest.IRestApiManager;
import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.request.IRestApiRequest;

/**
 *
 * @author evertonagilar
 */
public abstract class RestApiMetaQueryProvider extends RestApiProvider implements IRestApiProvider {
	private static final long serialVersionUID = 8651327535604802892L;

	public RestApiMetaQueryProvider() {
        super();
    }

    public RestApiMetaQueryProvider(Class entityClass) {
        super(entityClass);
    }

    public RestApiMetaQueryProvider(IRestApiManager apiManager, Class jsonClass, Class entityClass) {
        super(jsonClass, entityClass);
    }

    @Override
    public void validateRequestWithConstraints(final IRestApiRequest request) {
        super.validateRequestWithConstraints(request);
        if (!request.isParsed()) {
            if (request.getDataFormat() == RestApiDataFormat.ENTITY) {
                throw new RestApiConstraintException(RestApiConstraintException.METAQUERY_PROVIDER_NAO_SUPORTA_FORMATO_ENTITY);
            }
        }
    }


}
