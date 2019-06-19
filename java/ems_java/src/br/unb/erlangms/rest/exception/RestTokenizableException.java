/*
 * Copyright (c) 2019, CPD-UFSM. All Rights Reserved.
 */
package br.unb.erlangms.rest.exception;

/**
 *
 * @author evertonagilar
 */
public class RestTokenizableException extends RestApiException {
	private static final long serialVersionUID = 2822802374549739769L;

	public RestTokenizableException(String message) {
        super(message);
    }

}
