/*
 * 
 */
package br.erlangms.rest.serializer.dataset;

import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.schema.RestField;
import br.erlangms.rest.serializer.IRestApiSerializerStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author evertonagilar
 */
public class RestApiDataSetSerializer implements IRestApiSerializerStrategy {
    private ClientDataSetJson result;

    @Override
    public void execute(final IRestApiRequest request, final IRestApiProvider apiProvider, final Object data) {
        final List<ClientDataSetFieldDefinition> fieldsDefinitionDataSet = new ArrayList<>();
        final ArrayList resultData = (ArrayList) data;
        final List<RestField> fieldsRest = request.getFieldsList();
        final ClientDataSet clientDataSet = new ClientDataSet();
        ClientDataSetFieldDefinition clientDataSetFieldDefinition;

        for (RestField field : fieldsRest) {
            clientDataSetFieldDefinition = new ClientDataSetFieldDefinition(field);
            fieldsDefinitionDataSet.add(clientDataSetFieldDefinition);
        }

        //Vamos percorrer cada registro
        for (Object item : resultData) {
            Object object[] = ((Object[]) item);
            //Em cada um deles, vamos criar o respectivo registro no dataset
            ClientDataSetRecord record = clientDataSet.newRecord();
            for (int i = 0; i < object.length; i++) {
                //Sabemos que os campos do schema do provider deve estar ordenado com os dados retornados no array
                //Por isso, nos baseamos pelo seu indicador
                clientDataSetFieldDefinition = fieldsDefinitionDataSet.get(i);
                record.setColumn(clientDataSetFieldDefinition, object[i]);
            }
        }
        clientDataSet.setFieldsDefinition(fieldsDefinitionDataSet);
        result = new ClientDataSetJson(clientDataSet.serialize());
        
        
    }

    @Override
    public Object getData() {
        return result;
    }

    @Override
    public Integer getEstimatedSize() {
        return result.getData().length();
    }

}
