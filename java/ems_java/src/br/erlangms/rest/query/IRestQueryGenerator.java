/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.request.IRestApiRequest;
import java.io.Serializable;
import javax.persistence.Query;

/**
 *
 * @author evertonagilar
 */
public interface IRestQueryGenerator extends Serializable {

    /**
     * Cria uma query a partir de um filtro
     *
     * @param request requisição para criar a query
     * @return query com o filtro e a funÃ§Ã£o sql
     * @author Everton de Vargas Agilar
     */
    Query createQuery(final IRestApiRequest request);

}
