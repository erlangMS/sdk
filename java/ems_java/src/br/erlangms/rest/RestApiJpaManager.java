/*
 * 
 */
package br.erlangms.rest;

import br.erlangms.rest.exception.RestApiException;
import javax.persistence.EntityManager;

/**
 *
 * @author evertonagilar
 */
public class RestApiJpaManager extends RestApiManager {

    private EntityManager entityManager;

    public RestApiJpaManager() {
    }

    public RestApiJpaManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void checkDataLink() {
        if (entityManager == null){
            throw new RestApiException(RestApiException.NECESSARIO_INFORMAR_ENTITYMANAGER);
        }
    }

}
