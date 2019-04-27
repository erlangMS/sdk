/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.filter.RestFilterCondition;
import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
@FunctionalInterface
public interface RestJpaFilterGeneratorDefineIfParameterIsFromViewSql extends Serializable {
    public boolean defineIfParameterIsFromViewSql(final RestFilterCondition condition);
}
