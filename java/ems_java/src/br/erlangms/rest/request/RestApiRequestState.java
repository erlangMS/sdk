/*
 * 
 */
package br.erlangms.rest.request;

/**
 * Representa o estado que um RestApiRequest pode estar.
 *
 *      OPEN - indica que ela pode ser modificada
 *      PARSING - parser sendo realizado
 *      PARSED - neste estado, o RestApiRequest não pode ser modificado
 *
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public enum RestApiRequestState {
    OPEN,
    PARSING,
    PARSED
}
