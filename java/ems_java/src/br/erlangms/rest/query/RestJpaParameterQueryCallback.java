/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.filter.RestFilterCondition;
import java.io.Serializable;
import javax.persistence.Query;

/**
 *
 * @author evertonagilar
 */
@FunctionalInterface
public interface RestJpaParameterQueryCallback extends Serializable{
    public abstract Object execute(final RestFilterCondition condition, final Query query);
}
