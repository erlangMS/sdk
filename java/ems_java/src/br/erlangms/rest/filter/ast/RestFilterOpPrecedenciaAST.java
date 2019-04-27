package br.erlangms.rest.filter.ast;

import br.erlangms.rest.filter.IRestFilterASTGenerator;
import br.erlangms.rest.filter.IRestFilterASTVisitor;

public class RestFilterOpPrecedenciaAST extends RestFilterAST {

    protected RestFilterAST inner;

    public RestFilterOpPrecedenciaAST(RestFilterAST inner) {
        this.inner = inner;
    }

    @Override
    public String evaluate(final IRestFilterASTGenerator generator) {
        return "(" + inner.evaluate(generator) + ")";
    }

    @Override
    public void visit(final IRestFilterASTVisitor visitor){
        visitor.accept(this);
        inner.visit(visitor);
    }

}
