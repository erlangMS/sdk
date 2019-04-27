package br.erlangms.rest.test;

import javax.persistence.EntityManager;

import br.erlangms.rest.IRestApiManager;
import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.RestApiJpaManager;
import br.erlangms.rest.request.RestApiRequest;
import br.erlangms.rest.test.provider.AlunoRestProvider;

public class RestApiTest {

    public static void main(String[] args) {
        IRestApiManager apiManager;
        EntityManager entityManager;

        entityManager = null;

       
        // Primeiramente, vamos criar um RestEntityManager para criar um contexto para a API
        apiManager = new RestApiJpaManager(entityManager);

        // Vamos criar uma requisição RestApiRequest
        RestApiRequest request = new RestApiRequest();
        request.setFilter("{ \"nome__contains\" : \"AGILAR\" } and ({ \"tipo_sanguineo\" : \"AB\" } or { \"tipo_sanguineo\" : \"B\" })");
        request.setSort("id,   nome");
        request.setLimit(3);
        request.setOffset(0);
        request.setFields("id, nome");
        request.setDataFormat(RestApiDataFormat.VO);

        // vamos executar a consulta
        Object dados = apiManager.find(request, AlunoRestProvider.class);

    }

}
