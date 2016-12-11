package br.erlangms;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EmsNotFoundException extends RuntimeException {

	public EmsNotFoundException(final String message) {
        super(message);
    }
		
	private static final long serialVersionUID = 7613628452863123488L;
}
