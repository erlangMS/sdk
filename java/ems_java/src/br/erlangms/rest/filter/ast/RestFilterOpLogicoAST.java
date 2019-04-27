package br.erlangms.rest.filter.ast;

import br.erlangms.rest.filter.IRestFilterASTGenerator;
import br.erlangms.rest.filter.IRestFilterASTVisitor;

public abstract class RestFilterOpLogicoAST extends RestFilterAST {
	private static final long serialVersionUID = -2613920767712467004L;
	protected RestFilterAST left;
    protected RestFilterAST right;

    public RestFilterOpLogicoAST(RestFilterAST left, RestFilterAST right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String evaluate(final IRestFilterASTGenerator generator) {
        String code = super.evaluate(generator);
        return left.evaluate(generator) + code + right.evaluate(generator);
    }

    @Override
    public void visit(final IRestFilterASTVisitor visitor){
        left.visit(visitor);
        visitor.accept(this);
        right.visit(visitor);
    }


}
