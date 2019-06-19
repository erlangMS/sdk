package br.unb.erlangms;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EmsValidationException extends RuntimeException {
	private static final long serialVersionUID = -8316509235178192483L;
	private final List<String> errors;

	public EmsValidationException () {
		super();
		errors = new ArrayList<>();
	}

	public EmsValidationException (final String e) {
		super(e);
		errors = new ArrayList<>();
		errors.add(e);
	}


	public EmsValidationException (final List <String> l) {
		errors = l;
	}

	public void addError(final String error) {
		errors.add(error);
	}

	public List<String> getErrors () {
		return errors;
	}

}
