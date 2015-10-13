package br.erlangms;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class EmsRequestException extends RuntimeException {

	private static final long serialVersionUID = 2711905699673707319L;

	public EmsRequestException(String message) {
        super(message);
    }
		
}
