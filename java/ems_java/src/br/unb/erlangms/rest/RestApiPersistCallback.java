package br.unb.erlangms.rest;

import br.unb.erlangms.rest.request.IRestApiRequest;

/**
 *
 * @author evertonagilar
 */
public abstract class RestApiPersistCallback implements IRestApiPersistCallback {

    private final IRestApiRequest request;

    public RestApiPersistCallback(IRestApiRequest request) {
        this.request = request;
    }

    public IRestApiRequest getRequest(){
        return request;
    }

    @Override
    public abstract Long execute();


}
