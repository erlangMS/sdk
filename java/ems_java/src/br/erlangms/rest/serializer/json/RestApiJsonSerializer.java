/*
 * 
 */
package br.erlangms.rest.serializer.json;

import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.erlangms.rest.serializer.vo.RestApiVoSerializer;
import br.erlangms.rest.util.RestUtils;

/**
 * Classe que implementa a serialização para json
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 24/04/2019
 *
 */
public class RestApiJsonSerializer extends RestApiVoSerializer implements IRestApiSerializerStrategy {


    @Override
    public void execute(final IRestApiRequest request, final IRestApiProvider apiProvider, final Object data) {
        super.execute(request, apiProvider, data);
        this.data = RestUtils.toJson(this.data, true);
    }

    @Override
    public Integer getEstimatedSize() {
        return ((String)this.data).length();
    }

}
