package br.unb.erlangms.rest.provider;

import br.unb.erlangms.rest.schema.RestField;
import java.io.Serializable;

/**
 * Interface funcional para permitir modificar o valor de um campo durante a serialização.
 *
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 01/03/2019
 */
@FunctionalInterface
public interface RestFieldValueSerializeCallback extends Serializable{
    public abstract Object execute(final RestField field, final Object currentValue);
}
