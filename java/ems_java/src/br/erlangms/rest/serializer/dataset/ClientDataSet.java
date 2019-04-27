/*
 * 
 */
package br.erlangms.rest.serializer.dataset;

import br.erlangms.rest.exception.RestApiException;
import br.erlangms.rest.serializer.dataset.serializer.ClientDataSetXmlSerializer;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jader Adiel Schmitt
 *
 * Representa um TClientDataSet do delphi
 */
public class ClientDataSet {
    private List<ClientDataSetFieldDefinition> fields;
    private final List<ClientDataSetRecord> records;

    public ClientDataSet() {
        this.fields = new ArrayList<>();
        this.records = new ArrayList<>();
    }

    public void addFieldDefinition(final ClientDataSetFieldDefinition field) {
        if (fields.contains(field)) {
            throw new RestApiException("Atributo " + field.getName() + " jÃ¡ definido.");
        }
        fields.add(field);
    }

    public void setFieldsDefinition(final List<ClientDataSetFieldDefinition> fieldsDefinition) {
        this.fields = fieldsDefinition;
    }

    public ClientDataSetRecord newRecord() {
        ClientDataSetRecord record = new ClientDataSetRecord();
        records.add(record);
        return record;
    }

    public List<ClientDataSetRecord> getRecords() {
        return records;
    }

    public String serialize() {
        final ClientDataSetXmlSerializer serializer = new ClientDataSetXmlSerializer();
        serializer.serialize(fields, records);
        return serializer.toString();
    }

}
