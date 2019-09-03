package br.unb.erlangms.rest;

import java.io.Serializable;

/**
 * Callback definido para as operações de persistência dos verbos put e post.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 21/05/2019
 *
 */
@FunctionalInterface
public interface IRestApiPersistCallback extends Serializable {
    public abstract Long execute();
}
