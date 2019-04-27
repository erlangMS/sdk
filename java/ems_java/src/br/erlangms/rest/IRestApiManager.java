/*
 * 
 */
package br.erlangms.rest;

import java.io.Serializable;

import javax.persistence.EntityManager;

import br.erlangms.rest.exception.RestApiException;
import br.erlangms.rest.exception.RestApiNotFoundException;
import br.erlangms.rest.request.IRestApiRequest;

/**
 * Interface para o gestor da API RESTful
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 15/03/2019
 *
 */
public interface IRestApiManager extends Serializable {
    public EntityManager getEntityManager();
    public Object find(final IRestApiRequest request, final Class apiProviderClass) throws RestApiException;
    public Object findById(final IRestApiRequest request, final Class apiProviderClass) throws RestApiNotFoundException;
    public void checkDataLink();
}
