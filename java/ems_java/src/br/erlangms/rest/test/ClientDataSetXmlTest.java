/*
 * 
 */
package br.erlangms.rest.test;

import br.erlangms.rest.serializer.dataset.ClientDataSet;
import br.erlangms.rest.serializer.dataset.ClientDataSetDataType;
import br.erlangms.rest.serializer.dataset.ClientDataSetFieldDefinition;
import br.erlangms.rest.serializer.dataset.ClientDataSetRecord;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 *
 * @author jader
 */
public class ClientDataSetXmlTest {

    public static void main(String[] args) throws Exception {

        ClientDataSetFieldDefinition fieldTipoClasse = new ClientDataSetFieldDefinition("ID_TIPO_CLASSE", ClientDataSetDataType.INTEGER);
        ClientDataSetFieldDefinition fieldDescrClasse = new ClientDataSetFieldDefinition("DESCR_TIPO_CLASSE", ClientDataSetDataType.STRING, 100);
        ClientDataSetFieldDefinition fieldCodOperador = new ClientDataSetFieldDefinition("COD_OPERADOR", ClientDataSetDataType.INTEGER);
        ClientDataSetFieldDefinition fieldDataAlteracao = new ClientDataSetFieldDefinition("DT_ALTERACAO", ClientDataSetDataType.DATE);
        ClientDataSetFieldDefinition fieldHoraAlteracao = new ClientDataSetFieldDefinition("HR_ALTERACAO", ClientDataSetDataType.TIME);
        ClientDataSetFieldDefinition fieldConcorrencia = new ClientDataSetFieldDefinition("CONCORRENCIA", ClientDataSetDataType.INTEGER);

        ClientDataSet cds = new ClientDataSet();

        cds.addFieldDefinition(fieldTipoClasse);
        cds.addFieldDefinition(fieldDescrClasse);
        cds.addFieldDefinition(fieldCodOperador);
        cds.addFieldDefinition(fieldDataAlteracao);
        cds.addFieldDefinition(fieldHoraAlteracao);
        cds.addFieldDefinition(fieldConcorrencia);

        for (int i = 0; i < 100; i++) {
            ClientDataSetRecord record = cds.newRecord();
            record.setColumn(fieldTipoClasse, i);
            record.setColumn(fieldDescrClasse, "Grupo");
            record.setColumn(fieldCodOperador, 1);
            record.setColumn(fieldDataAlteracao, "20150922");
            record.setColumn(fieldHoraAlteracao, "15:59:47000");
            record.setColumn(fieldConcorrencia, 0);
        }

        System.out.println("XML");
        String xmlDataSet = cds.serialize();
        
        System.out.println(xmlDataSet);
    }

}
