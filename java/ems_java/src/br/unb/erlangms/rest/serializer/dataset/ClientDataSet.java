package br.unb.erlangms.rest.serializer.dataset;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.serializer.dataset.serializer.ClientDataSetXmlSerializer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jáder Adiél Schmitt
 *
 * Representa um TClientDataSet do delphi
 */
public class ClientDataSet {
    private List<RestField> fields;
    private final List<ClientDataSetRecord> records;

    public ClientDataSet() {
        this.fields = new ArrayList<>();
        this.records = new ArrayList<>();
    }
    public List<RestField> getFields() {
        return fields;
    }
    public void setFields(List<RestField> fields) {
        this.fields = fields;
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
