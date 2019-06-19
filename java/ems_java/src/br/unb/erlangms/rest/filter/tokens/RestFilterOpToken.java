package br.unb.erlangms.rest.filter.tokens;

import br.unb.erlangms.rest.filter.ast.RestFilterAST;

public abstract class RestFilterOpToken extends RestFilterToken {

    public RestFilterOpToken(String value, int position) {
        super(value, position);
    }

    public abstract RestFilterAST toExpression(RestFilterAST left, RestFilterAST right);
}
