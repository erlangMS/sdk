package br.unb.erlangms.rest.serializer.json;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.unb.erlangms.rest.serializer.vo.RestApiVoSerializer;
import br.unb.erlangms.rest.util.RestUtils;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 * Classe que implementa a serialização para json
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 24/04/2019
 *
 */
public class RestApiJsonSerializer extends RestApiVoSerializer implements IRestApiSerializerStrategy {
	private static final long serialVersionUID = 3391435476824697437L;

	@Override
    public void execute(final IRestApiRequestInternal request, final IRestApiProvider apiProvider, final Object data) {
        super.execute(request, apiProvider, data);
        this.data = RestUtils.toJson(this.data, true);
    }

    @Override
    public Integer getEstimatedSize() {
        return ((String)this.data).length();
    }

}
