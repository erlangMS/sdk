package br.erlangms.rest.filter.tokens;

import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.ast.RestFilterAndAST;

public class RestFilterAndToken extends RestFilterOpToken {

    public RestFilterAndToken(String value, int position) {
        super(value, position);
    }

    @Override
    public RestFilterAST toExpression(RestFilterAST left, RestFilterAST right) {
        return new RestFilterAndAST(left, right);
    }

}
