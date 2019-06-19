package br.unb.erlangms.rest.filter.tokens;

import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.filter.ast.RestFilterOrAST;

public class RestFilterOrToken extends RestFilterOpToken {

    public RestFilterOrToken(String value, int position) {
        super(value, position);
    }

    @Override
    public RestFilterAST toExpression(RestFilterAST left, RestFilterAST right) {
        return new RestFilterOrAST(left, right);
    }

}
