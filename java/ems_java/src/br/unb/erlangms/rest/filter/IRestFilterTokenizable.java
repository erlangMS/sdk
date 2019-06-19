package br.unb.erlangms.rest.filter;

import br.unb.erlangms.rest.filter.tokens.RestFilterToken;
import java.util.ArrayList;

/**
 * Interface para o analisador léxico do operador filter.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 26/03/2019
 *
 */
public interface IRestFilterTokenizable {

    public ArrayList<RestFilterToken> tokenize(char[] array);
}
