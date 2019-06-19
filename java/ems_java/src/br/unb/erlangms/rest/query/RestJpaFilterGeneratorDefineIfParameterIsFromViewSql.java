package br.unb.erlangms.rest.query;

import br.unb.erlangms.rest.filter.RestFilterCondition;
import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
@FunctionalInterface
public interface RestJpaFilterGeneratorDefineIfParameterIsFromViewSql extends Serializable {
    public boolean defineIfParameterIsFromViewSql(final RestFilterCondition condition);
}
