/*
 * 
 */
package br.erlangms.rest.serializer.dataset;

/**
 *
 * @author Jader Adiel Schmitt
 */
public class ClientDataSetColumn {
    private ClientDataSetFieldDefinition field;
    private Object value;
    public ClientDataSetFieldDefinition getField() {
        return field;
    }
    public void setField(ClientDataSetFieldDefinition field) {
        this.field = field;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
}
