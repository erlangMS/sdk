package br.unb.erlangms.rest.exception;

/**
 *
 * @author evertonagilar
 */
public class RestApiNotFoundException extends RestApiException {
	private static final long serialVersionUID = -5838828067775928968L;

	public RestApiNotFoundException(String message) {
        super(message);
    }

    public RestApiNotFoundException() {
        super("Recurso não existe.");
    }

}
