package br.unb.erlangms.rest.filter;

import br.unb.erlangms.rest.filter.ast.RestFilterAST;

/**
 * Interface para o gerador de código do operador filter.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 28/03/2019
 *
 */
public interface IRestFilterASTGenerator {
    public String emitCode(final RestFilterAST ast);
}
