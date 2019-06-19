package br.unb.erlangms.rest.serializer.entity;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 * Classe que implementa a serialização para entity JPA.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiEntitySerializer implements IRestApiSerializerStrategy {
	private static final long serialVersionUID = 69977971533557176L;
	private Object result;

    @Override
    public void execute(IRestApiRequestInternal request, IRestApiProvider apiProvider, Object data) {
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
