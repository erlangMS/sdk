/*
 * 
 */
package br.erlangms.rest.test;

import javax.persistence.EntityManager;

import br.erlangms.rest.IRestApiManager;
import br.erlangms.rest.RestApiJpaManager;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.request.RestApiRequest;
import br.erlangms.rest.test.provider.AlunoGetPorNomeRestProvider;

/**
 *
 * @author evertonagilar
 */
public class RestApiFindViewSqlTest {

    public static void main(String[] args) {
        IRestApiManager apiManager;
        EntityManager entityManager;

        entityManager = null;
        
        // Cria um RestEntityManager para enviar requisições RESTful para a camada de acesso a dados (DAO)
        apiManager = new RestApiJpaManager(entityManager);

        // Encapsula as informaÃ§Ãµes sobre uma chamada REST em um objeto RestApiRequest
        IRestApiRequest request = new RestApiRequest();
        request.setFilter("{ \"nome__contains\" : \"AGILAR\" } and ({ \"tipo_sanguineo\" : \"AB\" } or { \"tipo_sanguineo\" : \"B\" })");
        request.setSort("id,   nome");
        request.setLimit(3);
        request.setOffset(0);
        request.setFields("id, nome");

        Object result = apiManager.find(request, AlunoGetPorNomeRestProvider.class);

    }

}
