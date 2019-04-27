/*
 * 
 */
package br.erlangms.rest.serializer.entity;

import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.serializer.IRestApiSerializerStrategy;

/**
 * Classe que implementa a serialização para entity JPA.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiEntitySerializer implements IRestApiSerializerStrategy {
	private static final long serialVersionUID = 1944241111273359240L;
	private Object result;

    @Override
    public void execute(IRestApiRequest request, IRestApiProvider apiProvider, Object data) {
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
