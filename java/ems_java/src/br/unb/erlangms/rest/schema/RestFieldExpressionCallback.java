package br.unb.erlangms.rest.schema;

import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
@FunctionalInterface
public interface RestFieldExpressionCallback extends Serializable{
    public abstract Object execute();
}
