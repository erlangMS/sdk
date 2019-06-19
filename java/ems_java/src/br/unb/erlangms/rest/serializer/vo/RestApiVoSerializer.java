package br.unb.erlangms.rest.serializer.vo;

import br.unb.erlangms.rest.IRestApiEntity;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.serializer.IRestApiSerializerStrategy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.Entity;

/**
 * Classe que implementa a serialização para VO (Value Objects)
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiVoSerializer implements IRestApiSerializerStrategy {
    private static final long serialVersionUID = -4669003360673724639L;
    protected Object data;
    private int estimatedSize;
    private IRestApiProvider apiProvider;
    private IRestApiRequestInternal request;

    @Override
    public void execute(final IRestApiRequestInternal request, final IRestApiProvider apiProvider, final Object data) {
        this.apiProvider = apiProvider;
        this.request = request;
        this.estimatedSize = request.getLimit() + 4;
        this.data = toVo(data);
    }

    /**
     * Serializa uma entidade (Entity) ou lista de entidades ou arrays
     * de dados em um objeto que pode ser convertido em JSON.
     *
     *
     * @param obj entidade ou lista de entidades ou array de dados
     * @return Object
     * @author Everton de Vargas Agilar
     */
    public List toVo(final Object obj) {
        if (obj != null && request != null) {
            List<RestField> fieldNamesList = request.getFieldsList();

            // Se o objeto for um array, então vamos serializar uma lista de objetos
            if (obj instanceof ArrayList) {
                // Atribui nesta variável para  Não precisar tanto cast j� que sabemos que eh um array
                ArrayList list = (ArrayList) obj;

                // Se a lista for vazio, então vamos serializar uma lista vazia
                if (list.isEmpty()) {
                    return new ArrayList();
                }

                // Se a lista cont�m elementos do tipo Entity, então podemos chamar recursivamente
                // toVo para cada entidade do array
                if (list.get(0) instanceof Entity) {
                    List<Object> result = (List<Object>) list.stream()
                            .map(e -> toVo(e))
                            .collect(Collectors.toList());
                    estimatedSize += result.size() * 100;
                    return result;
                } else {
                    // Bom, j� que  não é um array de Entity, deve ser um array de array
                    if (fieldNamesList != null && (fieldNamesList.size() > 0)) {
                        // Quando � um array de arrays ou seja um array de registros e
                        // cada registro tem outro array com as colunas
                        List<Map<String, Object>> result = new ArrayList<>();
                        ArrayList<Object> listArray = list;
                        listArray.stream().map((record) -> {
                            Map<String, Object> object = new LinkedHashMap<>();
                            int fieldNamesListSize = fieldNamesList.size();
                            if (record != null && record.getClass().isArray()) {
                                for (int i = 0; i < ((Object[]) record).length; i++) {
                                    Object value = ((Object[]) record)[i];
                                    RestField field;
                                    if (fieldNamesListSize >= i + 1) {
                                        field = fieldNamesList.get(i);
                                    } else {
                                        //  Não inclui o atributo no objeto pois  não é para ser serializado
                                        break;
                                    }
                                    value = RestField.parseValue(field, value, false);
                                    object.put(field.getVoFieldName(), value);
                                    incrementEstimatedSizeFor(field, value);
                                }
                            } else {
                                Object value = record;
                                RestField field = fieldNamesList.get(0);
                                value = RestField.parseValue(field, value, false);
                                object.put(field.getVoFieldName(), value);
                                incrementEstimatedSizeFor(field, value);
                            }
                            return object;
                        }).forEach((object) -> {
                            result.add(object);
                        });
                        return result;
                    } else {
                        return new ArrayList();
                    }
                }
            } else {
                // obj  não é um array
                try {
                    try {
                        // se voClass for definido com uma classe que herda de JsonEntityMobile
                        // vamos chamar copyFromEntity para serialização definido pelo desenvolvedor
                        if (apiProvider.getVoClass() != null && apiProvider.getVoClass().newInstance() instanceof IRestApiEntity) {
                            IRestApiEntity jsonEntity = (IRestApiEntity) apiProvider.getVoClass().newInstance();
                            jsonEntity.copyFromEntity((Entity) obj);
                            List<IRestApiEntity> result = new ArrayList();
                            result.add(jsonEntity);
                            estimatedSize += result.size() * 100;
                            return result;
                        } else {
                            // se voClass  Não for uma classe que herda de JsonEntityMobile
                            // vamos usar o serializador baseado em remapSchema
                            Map<String, Object> map = new LinkedHashMap<>();
                            for (RestField field : fieldNamesList) {
                                Object value = field.getAttr().get(obj);
                                value = RestField.parseValue(field, value, false);
                                map.put(field.getVoFieldName(), value);
                                incrementEstimatedSizeFor(field, value);
                            }
                            List<Map<String, Object>> result = new ArrayList();
                            result.add(map);
                            return result;
                        }
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(RestApiVoSerializer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (SecurityException | IllegalArgumentException ex) {
                    throw new RestApiException(String.format(RestApiException.ERRO_CONVERTER_ENTIDADE_EM_VO, ex.getMessage()));
                }
            }
        }
        return null;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public Integer getEstimatedSize() {
        return estimatedSize;
    }

    private void incrementEstimatedSizeFor(final RestField field, final Object value) {
        estimatedSize = estimatedSize + field.getVoFieldName().length();
        if (value instanceof String) {
            estimatedSize += ((String) value).length();
        } else {
            estimatedSize += 4;
        }
    }


}
