/*
 * 
 */
package br.erlangms.rest.exception;

/**
 *
 * @author evertonagilar
 */
public class RestApiNotFoundException extends RestApiException {

    public RestApiNotFoundException(String message) {
        super(message);
    }

    public RestApiNotFoundException() {
        super("Recurso não existe.");
    }

}
