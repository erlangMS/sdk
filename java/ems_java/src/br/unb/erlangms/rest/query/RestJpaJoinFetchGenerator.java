package br.unb.erlangms.rest.query;

import br.unb.erlangms.rest.filter.IRestFilterASTVisitor;
import br.unb.erlangms.rest.filter.RestFilterCondition;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.schema.RestJoinType;

/**
 * Implementa o gerador de código dos fetchs join do operador filter para JPA
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 01/04/2019
 *
 */
public class RestJpaJoinFetchGenerator implements IRestFilterASTVisitor {
    private final IRestApiRequestInternal request;
    private final IRestApiProvider apiProvider;
    private final StringBuilder joinFetch;
    private final RestJoinType joinTypeFlag;

    public RestJpaJoinFetchGenerator(final IRestApiRequestInternal request) {
        this.request = request;
        this.apiProvider = request.getApiProvider();
        this.joinFetch = new StringBuilder();
        this.joinTypeFlag = request.getRequestUser().getFlags().getJoinType();
    }

    private String emitCodeJoinFetch(final RestFilterJsonAST ast) {
        for (RestFilterCondition condition : ast.getFilter()) {
            RestField field = condition.getField();
            if (field.isAttributeObject()) {
                if (joinTypeFlag != null) {
                    if (joinTypeFlag == RestJoinType.LEFT_JOIN) {
                        joinFetch.append(" left join this.");
                    } else if (joinTypeFlag == RestJoinType.RIGHT_JOIN) {
                        joinFetch.append(" right join this.");
                    } else {
                        joinFetch.append(" join this.");
                    }
                } else {
                    if (field.getJoinType() == RestJoinType.LEFT_JOIN) {
                        joinFetch.append(" left join this.");
                    } else if (field.getJoinType() == RestJoinType.RIGHT_JOIN) {
                        joinFetch.append(" right join this.");
                    } else {
                        joinFetch.append(" join this.");
                    }
                }
                joinFetch.append(field.getAttrBaseRelationName());
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

    public String getJoinFetchSmnt() {
        return joinFetch.toString();
    }
}
