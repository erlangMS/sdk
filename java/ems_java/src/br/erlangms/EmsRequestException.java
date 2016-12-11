package br.erlangms;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EmsRequestException extends RuntimeException {

	private static final long serialVersionUID = 2711905699673707319L;

	public EmsRequestException(final String message) {
        super(message);
    }
		
}
