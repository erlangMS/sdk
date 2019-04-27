/*
 * 
 */
package br.erlangms.rest.serializer.dataset.serializer;

import br.erlangms.rest.serializer.dataset.ClientDataSetColumn;
import br.erlangms.rest.serializer.dataset.ClientDataSetFieldDefinition;
import br.erlangms.rest.serializer.dataset.ClientDataSetRecord;
import java.util.List;

/**
 *
 * @author Jader Adiel Schmitt
 *
 * Classe responsavel por criar o xml que representa um ClientDataSet
 *
 */
public class ClientDataSetXmlSerializer {
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
    private final StringBuilder xml = new StringBuilder();
    public ClientDataSetXmlSerializer(){
        xml.append(XML_HEADER);
    }


 // Exemplo:
 // 
 // 
 // <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 // <DATAPACKET Version="2.0">
 //  <METADATA>
 //   <FIELDS>
 // 	<FIELD attrname="ID_TIPO_CLASSE" fieldtype="i4"/>
 //   </FIELDS>
 //  </METADATA>
 //  <ROWDATA>
 // 	<ROW COD_OPERADOR="1" CONCORRENCIA="0" DESCR_TIPO_CLASSE="Grupo" DT_ALTERACAO="20150922" HR_ALTERACAO="15:59:47000" ID_TIPO_CLASSE="1"/>
 //  </ROWDATA>
 // </DATAPACKET>
 //  

    public void serialize(final List<ClientDataSetFieldDefinition> fields, final List<ClientDataSetRecord> records) {
        xml.append("<DATAPACKET Version=\"2.0\">");
        xml.append("<METADATA>");
        xml.append("<FIELDS>");
        for (ClientDataSetFieldDefinition field : fields) {
            xml.append("<FIELD ");
            xml.append("attrname").append("=").append("\"").append(field.getName()).append("\" ");
            xml.append("fieldtype").append("=").append("\"").append(field.getDataType().getXmlType()).append("\" ");
            if (field.getSize() != null){
                xml.append("WIDTH").append("=").append("\"").append(field.getSize()).append("\" ");
            }
            xml.append("/>");
        }
        xml.append("</FIELDS>");
        xml.append("</METADATA>");
        xml.append("<ROWDATA>");
        for (ClientDataSetRecord record : records){
            xml.append("<ROW ");
            for (ClientDataSetColumn column : record.getColumns()){
                xml.append(column.getField().getName())
                        .append("=")
                        .append("\"")
                        .append(column.getValue())
                        .append("\" ");
            }
            xml.append("/>");
        }
        xml.append("</ROWDATA>");
        xml.append("</DATAPACKET>");
    }
    @Override
    public String toString(){
        return xml.toString();
    }

}
