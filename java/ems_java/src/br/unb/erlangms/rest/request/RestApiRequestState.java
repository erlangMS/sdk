package br.unb.erlangms.rest.request;

/**
 * Representa o estado que uma instância RestApiRequest pode estar.
 *
 * Os valores são:
 *
 *      OPEN -> indica que ela pode ser modificada e nenhuma validação que precise do provider vai ocorrer;
 *      PARSING -> está sendo realizada o parser e a validação da requisição e erros podem ocorrer;
 *      PARSED -> neste estado, o objeto não pode mais ser modificado a não ser que seja invalidado voltando para o estado OPEN.
 *
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public enum RestApiRequestState {
    OPEN,
    PARSING,
    PARSED
}
