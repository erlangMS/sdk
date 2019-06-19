package br.unb.erlangms.rest.provider;

import br.unb.erlangms.rest.contract.IRestApiContract;
import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.contract.RestApiVerb;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import java.util.List;

/**
 *
 * @author evertonagilar
 */
public abstract class RestApiMetaQueryProvider extends RestApiProvider implements IRestApiProvider {

    public RestApiMetaQueryProvider() {
        super();
    }

    @Override
    public void validateRequestWithContract(final IRestApiRequestInternal request) {
        super.validateRequestWithContract(request);
        if (!request.isParsed()) {
            if (request.getApiDataFormat() == RestApiDataFormat.ENTITY) {
                throw new RestApiException(RestApiException.METAQUERY_PROVIDER_NAO_SUPORTA_FORMATO_ENTITY);
            }
        }
    }

    @Override
    protected void afterCreateContract() {
        IRestApiContract contract = getContract();
        List<RestApiVerb> supportApiVerbs = contract.getSupportApiVerbs();
        supportApiVerbs.remove(RestApiVerb.POST);
        supportApiVerbs.remove(RestApiVerb.PUT);
        supportApiVerbs.remove(RestApiVerb.DELETE);
    }



}
