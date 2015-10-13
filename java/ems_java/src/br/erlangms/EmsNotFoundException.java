package br.erlangms;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class EmsNotFoundException extends RuntimeException {

	public EmsNotFoundException(String message) {
        super(message);
    }
		
	private static final long serialVersionUID = 7613628452863123488L;

}
