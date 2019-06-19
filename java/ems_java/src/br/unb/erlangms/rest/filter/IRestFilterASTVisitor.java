package br.unb.erlangms.rest.filter;

import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import java.io.Serializable;

/**
 * Interface para permitir visitar os nodes do AST do operador filter
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 28/03/2019
 *
 */
public interface IRestFilterASTVisitor extends Serializable {
    public Object accept(final RestFilterAST ast);
}
