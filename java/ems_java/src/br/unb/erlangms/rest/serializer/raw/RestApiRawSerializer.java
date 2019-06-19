package br.unb.erlangms.rest.serializer.raw;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 * Classe que implementa a serialização para raw data.
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiRawSerializer implements IRestApiSerializerStrategy {
	private static final long serialVersionUID = -5955551737150512446L;
	private Object result;

    @Override
    public void execute(final IRestApiRequestInternal request, final IRestApiProvider apiProvider, final Object data) {
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
