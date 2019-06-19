package br.unb.erlangms.rest.filter.ast;

public class RestFilterOrAST extends RestFilterOpLogicoAST {

    public RestFilterOrAST(RestFilterAST left, RestFilterAST right) {
        super(left, right);
    }

}
