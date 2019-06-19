package br.unb.erlangms.rest.serializer.dataset;

import br.unb.erlangms.rest.schema.RestField;

/**
 *
 * @author Jáder Adiél Schmitt
 */
public class ClientDataSetColumn {
    private RestField field;
    private Object value;
    public RestField getField() {
        return field;
    }
    public void setField(RestField field) {
        this.field = field;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
}
