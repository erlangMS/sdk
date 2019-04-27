/*
 * 
 */
package br.erlangms.rest.serializer.dataset.exception;

import br.erlangms.rest.schema.RestFieldType;

/**
 *
 * @author Jader Adiel Schmitt
 */

public class ClientDataSetDataTypeNotFound extends RuntimeException {
    public ClientDataSetDataTypeNotFound(final RestFieldType restFieldType) {
        super(String.format("Não foi possÃ­vel relacionar o tipo de campo REST '{0}' com um tipo correspondente para um ClientDataSet.",  restFieldType));
    }
}


