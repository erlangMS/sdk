/*
 * 
 */
package br.erlangms.rest.provider;

import java.io.Serializable;

/**
 * Interface funcional para permitir modificar o valor de um campo durante a serialização no provider.
 *
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 01/03/2019
 */
@FunctionalInterface
public interface RestFieldValueSerializeTransform extends Serializable{
    public abstract Object transform(final String fieldName, final Object currentValue);
}
