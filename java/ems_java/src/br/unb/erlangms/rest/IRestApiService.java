package br.unb.erlangms.rest;

import br.unb.erlangms.rest.request.IRestApiRequest;
import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
public interface IRestApiService extends Serializable {

    /**
     * Retorna uma lista de objetos a partir de um RestApiRequest
     *
     * @param request
     * @return lista dos objetos
     * @author Everton de Vargas Agilar
     */
    public Object find(final IRestApiRequest request);

    /**
     * Retorna um objeto específico a partir de um RestApiRequest. Se o objeto não existe, retorna RestApiNotFoundException.
     *
     * @param request
     * @return lista dos objetos
     * @author Everton de Vargas Agilar
     */
    public Object findById(final IRestApiRequest request);

    /**
     * Permite atualizar um objeto específico. Se o objeto não existe, retorna RestApiNotFoundException.
     *
     * @param request
     * @return objeto ou lista de atributos do objeto atualizado.
     * @author Everton de Vargas Agilar
     */
    public Object put(final IRestApiRequest request);

    /**
     * Permite cadastrar um objeto.
     *
     * @param request
     * @return objeto ou lista de atributos do objeto cadastrado.
     * @author Everton de Vargas Agilar
     */
    public Object post(final IRestApiRequest request);

    /**
     * Método responsável por persistir um objeto.
     *
     * @param request
     * @author Everton de Vargas Agilar
     */
    public void persist(IRestApiRequest request);

    /**
     * Retorna a classe do provedor do serviço.
     *
     * @return RestApiProvider
     * @author Everton de Vargas Agilar
     */
    public Class getRestApiProviderClass();


}
