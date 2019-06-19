package br.unb.erlangms.rest.filter.ast;

public class RestFilterAndAST extends RestFilterOpLogicoAST {

    public RestFilterAndAST(RestFilterAST left, RestFilterAST right) {
        super(left, right);
    }

}
