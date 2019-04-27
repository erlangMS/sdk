/*
 * 
 */
package br.erlangms.rest.serializer.raw;

import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.serializer.IRestApiSerializerStrategy;

/**
 * Classe que implementa a serialização para raw data.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiRawSerializer implements IRestApiSerializerStrategy {
    private Object result;

    @Override
    public void execute(final IRestApiRequest request, final IRestApiProvider apiProvider, final Object data) {
        result = data;
    }

    @Override
    public Object getData() {
        return result;
    }

    @Override
    public Integer getEstimatedSize() {
        return null;
    }

}
