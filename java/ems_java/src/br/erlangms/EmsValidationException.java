package br.erlangms;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public final class EmsValidationException extends RuntimeException {
	private static final long serialVersionUID = -8316509235178192483L;
	private List<String> errors;

	public EmsValidationException() {
		super();
		errors = new ArrayList<>();
	}

	public EmsValidationException(final String e) {
		super(e);
		errors = new ArrayList<>();
		errors.add(e);
	}

	public EmsValidationException(final List<String> l) {
		super(buildMessage(l));
		errors = l != null ? l : new ArrayList<>();
	}

	public void addError(final String error) {
		errors.add(error);
	}

	public List<String> getErrors() {
		return errors;
	}

	/**
	 * Retorna true se houver ao menos um erro registrado.
	 */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	/**
	 * Concatena todos os erros da lista em uma única mensagem,
	 * garantindo que getMessage() nunca retorne null.
	 */
	@Override
	public String getMessage() {
		if (errors == null || errors.isEmpty()) {
			String superMsg = super.getMessage();
			return superMsg != null ? superMsg : "Erro de validação.";
		}
		return buildMessage(errors);
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getMessage();
	}

	private static String buildMessage(final List<String> erros) {
		if (erros == null || erros.isEmpty()) {
			return "Erro de validação.";
		}
		StringBuilder sb = new StringBuilder();
		for (String e : erros) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(e);
		}
		return sb.toString();
	}

}
