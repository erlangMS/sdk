package br.unb.erlangms.rest.serializer;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import java.io.Serializable;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 * Interface para o subsistema de serialização da API REST
 *
 * Classes que implementam esta interface devem realizar a serialização
 * de acordo com o formato definido por RestApiDataFormat.
 *
 * O Serializador pode ter a capacidade de estimar o tamanho do
 * dado após a etapa de serialização. Se não for possível
 * estimar o tamanho, devem retornar null em getEstimatedSize().
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public interface IRestApiSerializerStrategy extends Serializable {
    public void execute(final IRestApiRequestInternal request,
                           final IRestApiProvider apiProvider,
                           final Object data);
    public Object getData();
    public Integer getEstimatedSize();
}
