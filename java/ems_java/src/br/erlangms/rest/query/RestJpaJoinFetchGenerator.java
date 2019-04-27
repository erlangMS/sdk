/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.filter.IRestFilterASTVisitor;
import br.erlangms.rest.filter.RestFilterCondition;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.erlangms.rest.provider.IRestApiProvider;

/**
 * Implementa o gerador de código dos fetchs join do operador filter para JPA
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 01/04/2019
 *
 */
public class RestJpaJoinFetchGenerator implements IRestFilterASTVisitor {

    private final IRestApiProvider apiProvider;
    private final StringBuilder joinFetch;

    public RestJpaJoinFetchGenerator(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        this.joinFetch = new StringBuilder();
    }

    private String emitCodeJoinFetch(final RestFilterJsonAST ast) {
        for (RestFilterCondition condition : ast.getFilter()) {
                if (condition.getField().isAttributeObject()) {
                    joinFetch.append(" join fetch this.");
                    joinFetch.append(condition.getField().getAttrBaseRelationName());
                    joinFetch.append(" ");
                }
        }
        return joinFetch.toString();
    }

    @Override
    public Object accept(RestFilterAST ast) {
        if (ast instanceof RestFilterJsonAST) {
            return emitCodeJoinFetch((RestFilterJsonAST) ast);
        }
        return "";
    }


    public String getJoinFetchSmnt(){
        return joinFetch.toString();
    }
}
