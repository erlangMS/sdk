package br.unb.erlangms.rest.serializer.dataset.exception;

import br.unb.erlangms.rest.schema.RestFieldType;

/**
 *
 * @author Jáder Adiél Schmitt
 */

public class ClientDataSetDataTypeNotFound extends RuntimeException {
    public ClientDataSetDataTypeNotFound(final RestFieldType restFieldType) {
        super(String.format("Não foi possível relacionar o tipo de campo REST '{0}' com um tipo correspondente para um ClientDataSet.",  restFieldType));
    }
}


