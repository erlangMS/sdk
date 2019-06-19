package br.unb.erlangms.rest;

import br.unb.erlangms.rest.exception.RestApiNotFoundException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequest;
import java.io.Serializable;
import javax.persistence.EntityManager;

/**
 * Interface para o gestor da API RESTful
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 15/03/2019
 *
 */
public interface IRestApiManager extends Serializable {
    public EntityManager getEntityManager();
    public Object find(final IRestApiRequest request, final Class apiProviderClass);
    public Object findById(final IRestApiRequest request, final Class apiProviderClass) throws RestApiNotFoundException;
    public Object put(final IRestApiRequest request, final Class apiProviderClass, final RestApiPersistCallback persistCallback) throws RestApiNotFoundException;
    public Object post(final IRestApiRequest request, final Class apiProviderClass, final RestApiPersistCallback persistCallback);
    public boolean canExecute(final IRestApiRequest request, final IRestApiProvider apiProvider);
}
