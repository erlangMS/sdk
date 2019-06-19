package br.unb.erlangms.rest.query;

import java.io.Serializable;
import javax.persistence.Query;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 *
 * @author evertonagilar
 */
public interface IRestQueryGenerator extends Serializable {

    /**
     * Cria uma query a partir de um filtro
     *
     * @param request	objeto da requisição
     * @return query com o filtro e a função sql
     * @author Everton de Vargas Agilar
     */
    Query createQuery(final IRestApiRequestInternal request);

}
