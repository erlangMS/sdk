package br.unb.erlangms.rest.serializer.dataset;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.serializer.IRestApiSerializerStrategy;
import java.util.ArrayList;
import java.util.List;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 *
 * @author evertonagilar
 */
public class RestApiDataSetSerializer implements IRestApiSerializerStrategy {
    private ClientDataSetJson result;

    @Override
    public void execute(final IRestApiRequestInternal request, final IRestApiProvider apiProvider, final Object data) {
        final ArrayList resultData = (ArrayList) data;
        final List<RestField> fieldsRest = request.getFieldsList();
        final ClientDataSet clientDataSet = new ClientDataSet();
        RestField restField;
       
        //Vamos percorrer cada registro
        for (Object item : resultData) {
            Object object[] = ((Object[]) item);
            //Em cada um deles, vamos criar o respectivo registro no dataset
            ClientDataSetRecord record = clientDataSet.newRecord();
            for (int i = 0; i < object.length; i++) {
                //Sabemos que os campos do schema do provider deve estar ordenado com os dados retornados no array
                //Por isso, nos baseamos pelo seu indicador
                restField = fieldsRest.get(i);
                record.setColumn(restField, object[i]);
            }
        }
        clientDataSet.setFields(fieldsRest);
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
