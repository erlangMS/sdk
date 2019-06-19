package br.unb.erlangms.rest.filter;

import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.filter.tokens.RestFilterToken;
import java.util.List;

/**
 * Interface para o parser do operador filter
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 26/03/2019
 *
 */
public interface IRestFilterParser {
    RestFilterAST parse(List<RestFilterToken> tokens);
}