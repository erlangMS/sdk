package br.unb.erlangms.rest.serializer.dataset.serializer;

import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.serializer.dataset.ClientDataSetColumn;
import br.unb.erlangms.rest.serializer.dataset.ClientDataSetDataType;
import br.unb.erlangms.rest.serializer.dataset.ClientDataSetRecord;
import java.util.List;

/**
 *
 * @author Jáder Adiél Schmitt
 *
 * Classe responsavel por criar o xml que representa um ClientDataSet
 *
 */
public class ClientDataSetXmlSerializer {
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
    private final StringBuilder xml = new StringBuilder();
    public ClientDataSetXmlSerializer() {
        xml.append(XML_HEADER);
    }

    public void serialize(final List<RestField> fields, final List<ClientDataSetRecord> records) {
        xml.append("<DATAPACKET Version=\"2.0\">");
        xml.append("<METADATA>");
        xml.append("<FIELDS>");
        for (RestField field : fields) {
            xml.append("<FIELD ");
            xml.append("attrname").append("=").append("\"").append(field.getVoFieldName()).append("\" ");
            ClientDataSetDataType clientDataSetDataType = ClientDataSetDataType.fromRestApiType(field.getFieldType());
            xml.append("fieldtype").append("=").append("\"").append(clientDataSetDataType.getXmlType()).append("\" ");
            Integer fieldLength = field.getFieldLength();
            if (fieldLength != null) {
                xml.append("WIDTH").append("=").append("\"").append(fieldLength).append("\" ");
            }
            xml.append("/>");
        }
        xml.append("</FIELDS>");
        xml.append("</METADATA>");
        xml.append("<ROWDATA>");
        for (ClientDataSetRecord record : records) {
            xml.append("<ROW ");
            for (ClientDataSetColumn column : record.getColumns()) {
                RestField field = column.getField();
                xml.append(field.getVoFieldName())
                        .append("=")
                        .append("\"");
                Object value = column.getValue();
                Object valueFormatted;
                switch (field.getFieldType()) {
                    case STRING:
                        valueFormatted = field.parseAsString(value, false);
                        break;
                    case INTEGER:
                        valueFormatted = field.parseValueAsInteger(value, false);
                        break;
                    case BOOLEAN:
                        valueFormatted = field.parseValueAsBoolean(value);
                        break;
                    case DOUBLE:
                        valueFormatted = field.parseValueAsDouble(value, false);
                        break;
                    case LONG:
                        valueFormatted = field.parseValueAsLong(value, false);
                        break;
                    default:
                        valueFormatted = value;
                        break;
                }
                xml.append(valueFormatted);
                xml.append("\" ");
            }
            xml.append("/>");
        }
        xml.append("</ROWDATA>");
        xml.append("</DATAPACKET>");
    }
    @Override
    public String toString() {
        return xml.toString();
    }

}
