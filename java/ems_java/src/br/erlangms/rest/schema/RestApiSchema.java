/*
 * 
 */
package br.erlangms.rest.schema;

import br.erlangms.rest.contract.IRestApiContract;
import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.provider.IRestApiProvider;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.reflect.FieldUtils;

/**
 * Classe de implementação da interface IRestApiSchema
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 22/04/2019
 *
 */
public class RestApiSchema implements IRestApiSchema {
    private final List<RestField> fieldsList;
    private final IRestApiContract owner;

    public RestApiSchema(IRestApiContract owner) {
        this.owner = owner;
        this.fieldsList = new ArrayList<>();
    }

    @Override
    public List<RestField> getFieldsList() {
        return Collections.unmodifiableList(fieldsList);
    }

    @Override
    public String getVoFields() {
        return fieldsList.stream()
                .map(f -> f.getVoFieldName())
                .collect(Collectors.joining(","));
    }

    private RestField addField(final String voFieldName, final String fieldName, final RestFieldType fieldType, Integer fieldLength, boolean filterRequired) {
        RestField field = new RestField();
        field.setVoFieldName(voFieldName);
        field.setFieldName(fieldName);
        field.setFieldType(fieldType);
        field.setFieldLength(fieldLength);
        IRestApiProvider apiProvider = owner.getApiProvider();
        if (owner.getApiProvider().getViewSql() == null) {
            Field attr;
            // O nome do campo pode ter ".", ou seja, pode ser um atributo de objeto
            if (fieldName.contains(".")) {
                Field attrBase;
                String[] fieldNameParts = fieldName.split("\\.");
                String attrBaseRelationName = fieldNameParts[0];
                attrBase = FieldUtils.getField(apiProvider.getEntityClass(), attrBaseRelationName, true);
                if (attrBase != null) {
                    attr = FieldUtils.getField(attrBase.getType(), fieldNameParts[1], true);
                    if (attr == null) {
                        throw new RestApiConstraintException(String.format(RestApiConstraintException.ATRIBUTO_IN_ENTITY_NAO_EXISTE, fieldName));
                    }
                    field.setAttrBase(attrBase);
                    field.setAttrBaseRelationName(attrBaseRelationName);
                } else {
                    throw new RestApiConstraintException(String.format(RestApiConstraintException.ATRIBUTO_IN_ENTITY_NAO_EXISTE, fieldName));
                }
            } else {
                attr = FieldUtils.getField(apiProvider.getEntityClass(), fieldName, true);
                if (attr == null) {
                    throw new RestApiConstraintException(String.format(RestApiConstraintException.ATRIBUTO_IN_ENTITY_NAO_EXISTE, fieldName));
                }
            }
            field.setAttr(attr);
        }
        fieldsList.add(field);
        return field;
    }

    @Override
    public Optional<RestField> getFieldByVoName(final String voFieldName) {
        String voFieldName2 = voFieldName.equals("pk") ? "id" : voFieldName;
        return fieldsList.stream().
                filter(f -> f.getVoFieldName().equals(voFieldName2)).
                findFirst();
    }

    @Override
    public RestField addFieldAsInteger(final String voFieldName, final String fieldName) {
        return addField(voFieldName, fieldName, RestFieldType.INTEGER, null, false);
    }

    @Override
    public RestField addFieldAsDouble(String voFieldName, String fieldName) {
        return addField(voFieldName, fieldName, RestFieldType.DOUBLE, null, false);
    }

    @Override
    public RestField addFieldAsDate(String voFieldName, String fieldName) {
        return addField(voFieldName, fieldName, RestFieldType.DATE, null, false);
    }

    @Override
    public RestField addFieldAsTime(String voFieldName, String fieldName) {
        return addField(voFieldName, fieldName, RestFieldType.TIME, null, false);
    }

    @Override
    public RestField addFieldAString(String voFieldName, String fieldName, Integer fieldLength) {
        return addField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, false);
    }

    @Override
    public RestField addFieldAsInteger(String voFieldName, String fieldName, boolean filterRequired) {
        return addField(voFieldName, fieldName, RestFieldType.INTEGER, null, filterRequired);
    }

    @Override
    public RestField addFieldAsDouble(String voFieldName, String fieldName, boolean filterRequired) {
        return addField(voFieldName, fieldName, RestFieldType.DOUBLE, null, filterRequired);
    }

    @Override
    public RestField addFieldAsDate(String voFieldName, String fieldName, boolean filterRequired) {
        return addField(voFieldName, fieldName, RestFieldType.DATE, null, filterRequired);
    }

    @Override
    public RestField addFieldAsTime(String voFieldName, String fieldName, boolean filterRequired) {
        return addField(voFieldName, fieldName, RestFieldType.TIME, null, filterRequired);
    }

    @Override
    public RestField addFieldAString(String voFieldName, String fieldName, Integer fieldLength, boolean filterRequired) {
        return addField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, filterRequired);
    }

}
