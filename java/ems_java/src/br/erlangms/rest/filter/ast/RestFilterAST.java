package br.erlangms.rest.filter.ast;

import br.erlangms.rest.filter.IRestFilterASTGenerator;
import br.erlangms.rest.filter.IRestFilterASTVisitor;
import java.io.Serializable;

public abstract class RestFilterAST implements Serializable {

    public String evaluate(final IRestFilterASTGenerator generator) {
        return generator.emitCode(this);
    }

    public void visit(final IRestFilterASTVisitor visitor){
        visitor.accept(this);
    }

}
