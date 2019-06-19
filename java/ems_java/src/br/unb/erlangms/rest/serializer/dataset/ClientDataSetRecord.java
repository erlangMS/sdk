package br.unb.erlangms.rest.serializer.dataset;

import br.unb.erlangms.rest.schema.RestField;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jader
 */
public class ClientDataSetRecord {

    private final List<ClientDataSetColumn> columns = new ArrayList<>();

    public List<ClientDataSetColumn> getColumns() {
        return columns;
    }

    public ClientDataSetColumn setColumn(RestField field, Object value){
        ClientDataSetColumn column = new ClientDataSetColumn();
        column.setField(field);
        column.setValue(value);
        this.columns.add(column);
        return column;
    }

}
