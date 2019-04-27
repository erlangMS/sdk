package br.erlangms.rest.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class RestApiException extends RuntimeException {
    public final static String NECESSARIO_INFORMAR_ENTITYMANAGER = "Necessário informar o EntityManager.";

    public RestApiException(String message) {
        super(message);
    }
}
