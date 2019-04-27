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
public interface RestJpaFilterGeneratorEmitCodeTransform extends Serializable{
    public String emitCode(final RestFilterCondition condition,
                            final String codeGenerated);
}
