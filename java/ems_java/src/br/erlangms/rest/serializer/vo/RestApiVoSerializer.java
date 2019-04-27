/*
 * 
 */
package br.erlangms.rest.serializer.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import br.erlangms.rest.IRestApiEntity;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.schema.RestField;
import br.erlangms.rest.serializer.IRestApiSerializerStrategy;
import br.erlangms.rest.util.StringEnum;

/**
 * Classe que implementa a serialização para VO (Value Objects)
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiVoSerializer implements IRestApiSerializerStrategy {
	private static final long serialVersionUID = -2623706211069292822L;
	protected Object data;
    private int estimatedSize;
    private IRestApiProvider apiProvider;
    private IRestApiRequest request;

    @Override
    public void execute(final IRestApiRequest request, final IRestApiProvider apiProvider, final Object data) {
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
                // Atribui nesta variÃ¡vel para não precisar tanto cast jÃ¡ que sabemos que eh um array
                ArrayList list = (ArrayList) obj;

                // Se a lista for vazio, então vamos serializar uma lista vazia
                if (list.isEmpty()) {
                    return new ArrayList();
                }

                // Se a lista contÃ©m elementos do tipo Entity, então podemos chamar recursivamente
                // toVo para cada entidade do array
                if (list.get(0) instanceof Entity) {
                    List<Object> result = (List<Object>) list.stream()
                            .map(e -> toVo(e))
                            .collect(Collectors.toList());
                    estimatedSize += result.size() * 100;
                    return result;
                } else {
                    // Bom, jÃ¡ que não Ã© um array de Entity, deve ser um array de array
                    if (fieldNamesList != null && (fieldNamesList.size() > 0)) {
                        // Quando Ã© um array de arrays ou seja um array de registros e
                        // cada registro tem outro array com as colunas
                        List<Map<String, Object>> result = new ArrayList<>();
                        ArrayList<Object> listArray = list;
                        listArray.stream().map((record) -> {
                            Map<String, Object> object = new LinkedHashMap<>();
                            if (record != null && record.getClass().isArray()) {
                                for (int i = 0; i < ((Object[]) record).length; i++) {
                                    Object value = ((Object[]) record)[i];
                                    String fieldName;
                                    if (fieldNamesList.size() >= i + 1) {
                                        fieldName = fieldNamesList.get(i).getVoFieldName();
                                    } else {
                                        // Tinha pensado em gerar um fieldName mas eh melhor não incluir o atributo
                                        //fieldName = "atributo_" + Integer.toString(i);

                                        // Não inclui o atributo no objeto pois não Ã© para ser serializado
                                        break;
                                    }
                                    if (value != null && value instanceof StringEnum) {
                                        StringEnum valueEnum = (StringEnum) value;
                                        if (apiProvider.getFieldVoTransform() != null) {
                                            value = apiProvider.getFieldVoTransform().transform(fieldName, valueEnum.getValue());
                                        } else {
                                            value = valueEnum.getValue();
                                        }
                                        object.put(fieldName, value);
                                        incrementEstimatedSizeFor(fieldName, value);
                                    } else {
                                        if (apiProvider.getFieldVoTransform() != null) {
                                            value = apiProvider.getFieldVoTransform().transform(fieldName, value);
                                        }
                                        object.put(fieldName, value);
                                        incrementEstimatedSizeFor(fieldName, value);
                                    }
                                }
                            } else {
                                Object value = record;
                                String voFieldName = fieldNamesList.get(0).getVoFieldName();
                                if (value != null && value instanceof StringEnum) {
                                    StringEnum valueEnum = (StringEnum) value;
                                    object.put(voFieldName, valueEnum.getValue());
                                } else {
                                    object.put(voFieldName, value);
                                }
                                incrementEstimatedSizeFor(voFieldName, value);
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
                // obj não Ã© um array
                try {
                    try {
                        // se voClass for definido com uma classe que herda de JsonEntityMobile
                        // vamos chamar copyFromEntity para serialização definido pelo desenvolvedor
                        if (apiProvider.getVoClass() != null && apiProvider.getVoClass().isAnnotationPresent(IRestApiEntity.class)) {
                        	IRestApiEntity jsonEntity = (IRestApiEntity) apiProvider.getVoClass().newInstance();
                            jsonEntity.copyFromEntity((Entity) obj);
                            List<IRestApiEntity> result = new ArrayList<>();
                            result.add(jsonEntity);
                            estimatedSize += result.size() * 100;
                            return result;
                        } else {
                            // se voClass não for uma classe que herda de JsonEntityMobile
                            // vamos usar o serializador baseado em remapSchema
                            Map<String, Object> map = new LinkedHashMap<>();
                            for (RestField field : fieldNamesList) {
                                Object value = field.getAttr().get(obj);
                                String voFieldName = field.getVoFieldName();
                                if (value != null && value instanceof StringEnum) {
                                    StringEnum valueEnum = (StringEnum) value;
                                    map.put(voFieldName, valueEnum.getValue());
                                } else {
                                    map.put(voFieldName, value);
                                }
                                incrementEstimatedSizeFor(voFieldName, value);
                            }
                            List<Map<String, Object>> result = new ArrayList();
                            result.add(map);
                            return result;
                        }
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(RestApiVoSerializer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (SecurityException | IllegalArgumentException ex) {
                    throw new RestApiConstraintException(String.format(RestApiConstraintException.ERRO_CONVERTER_ENTIDADE_EM_VO, ex.getMessage()));
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

    private void incrementEstimatedSizeFor(String fieldName, Object value) {
        estimatedSize = estimatedSize + fieldName.length();
        if (value instanceof String) {
            estimatedSize += ((String) value).length();
        } else {
            estimatedSize += 4;
        }
    }

}
