package br.erlangms.rest.filter;

import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.tokens.RestFilterToken;
import java.util.List;

/**
 * Interface para o parser do operador filter
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 26/03/2019
 *
 */
public interface IRestFilterParser {
    RestFilterAST parse(List<RestFilterToken> tokens);
}
