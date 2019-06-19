package br.unb.erlangms.rest.serializer;

import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.serializer.dataset.RestApiDataSetSerializer;
import br.unb.erlangms.rest.serializer.entity.RestApiEntitySerializer;
import br.unb.erlangms.rest.serializer.json.RestApiJsonSerializer;
import br.unb.erlangms.rest.serializer.raw.RestApiRawSerializer;
import br.unb.erlangms.rest.serializer.vo.RestApiVoSerializer;

/**
 * Fábrica de classes de serialização para os formatos do enum RestApiDataFormat.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public final class RestApiSerializerFactory {

    public static IRestApiSerializerStrategy createInstance(RestApiDataFormat dataFormat) {
        switch (dataFormat) {
            case RAW:
                return new RestApiRawSerializer();
            case ENTITY:
                return new RestApiEntitySerializer();
            case DATASET:
                return new RestApiDataSetSerializer();
            case JSON:
                return new RestApiJsonSerializer();
            default:
                return new RestApiVoSerializer();
        }
    }

}
