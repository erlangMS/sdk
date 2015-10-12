package br.erlangms;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class EmsValidation extends RuntimeException {
	private static final long serialVersionUID = -8316509235178192483L;
	private List<String> errors;
	
	public EmsValidation () {
		errors = new ArrayList<>();
	}
	
	public EmsValidation (String e) {
		this ();
		errors.add(e);
	}
	
	
	public EmsValidation (List <String> l) {
		errors = l;
	}
	
	public void addError(String error) {
		errors.add(error);
	}
	
	public List<String> getErrors () {
		return errors;
	}
	
}
