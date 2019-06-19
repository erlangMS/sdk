package br.unb.erlangms.rest.query;

import br.unb.erlangms.rest.filter.RestFilterCondition;
import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
@FunctionalInterface
public interface RestJpaFilterGeneratorEmitCodeCallback extends Serializable{
    public String execute(final RestFilterCondition condition,
                           final String codeGenerated);
}
