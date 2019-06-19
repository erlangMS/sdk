package br.unb.erlangms.rest.schema;

import br.unb.erlangms.rest.contract.IRestApiContract;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
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
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 22/04/2019
 *
 */
public class RestApiSchema implements IRestApiSchema {
    private final List<RestField> fieldsList;
    private final IRestApiContract contract;

    public RestApiSchema(IRestApiContract contract) {
        this.contract = contract;
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

    @Override
    public List<RestField> getRequiredFieldsList() {
        return fieldsList.stream()
                .filter(f -> f.isFilterRequired())
                .collect(Collectors.toList());
    }

    private RestField createField(final String voFieldName,
                                  final String fieldName,
                                  final RestFieldType fieldType,
                                  Integer fieldLength,
                                  boolean filterRequired) {

        if (voFieldName == null || voFieldName.trim().isEmpty()) {
            throw new RestApiException(RestApiException.NOME_ATRIBUTO_VO_OBRIGATORIO_SCHEMA);
        }

        if (voFieldName.length() < 2 || voFieldName.length() > 60 || !voFieldName.matches("[a-zA-z]+[a-zA-Z_]*")) {
            throw new RestApiException(RestApiException.ATRIBUTO_INVALIDO_SCHEMA, voFieldName, getClass().getSimpleName());
        }

        if (fieldName == null || fieldName.trim().length() < 2 || voFieldName.length() > 60) {
            throw new RestApiException(RestApiException.ATRIBUTO_INVALIDO_SCHEMA, voFieldName, getClass().getSimpleName());
        }

        // Não permite atributo duplicado no schema
        if (fieldsList.stream()
                .filter(f -> f.getVoFieldName().equals(voFieldName))
                .findAny()
                .isPresent()) {
            throw new RestApiException(RestApiException.ATRIBUTO_DUPLICADO_SCHEMA, voFieldName, getClass().getSimpleName());
        }

        RestField field = new RestField(this, voFieldName, fieldName, fieldType);
        field.setFieldLength(fieldLength);
        field.setFilterRequired(filterRequired);
        IRestApiProvider apiProvider = contract.getApiProvider();
        if (contract.getApiProvider().getViewSql() == null) {
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
                        throw new RestApiException(String.format(RestApiException.ATRIBUTO_IN_ENTITY_NAO_EXISTE, fieldName));
                    }
                    field.setAttrBase(attrBase);
                    field.setAttrBaseRelationName(attrBaseRelationName);
                } else {
                    throw new RestApiException(String.format(RestApiException.ATRIBUTO_IN_ENTITY_NAO_EXISTE, fieldName));
                }
            } else {
                attr = FieldUtils.getField(apiProvider.getEntityClass(), fieldName, true);
                if (attr == null) {
                    throw new RestApiException(String.format(RestApiException.ATRIBUTO_IN_ENTITY_NAO_EXISTE, fieldName));
                }
            }
            field.setAttr(attr);
        }
        fieldsList.add(field);
        return field;
    }

    private RestField createFieldExpression(final String voFieldName,
                                            final RestFieldExpressionCallback callback) {
        if (voFieldName == null || voFieldName.trim().isEmpty()) {
            throw new RestApiException(RestApiException.NOME_ATRIBUTO_VO_OBRIGATORIO_SCHEMA);
        }

        if (voFieldName.length() < 2 || voFieldName.length() > 60 || !voFieldName.matches("[a-zA-z]+[a-zA-Z_]*")) {
            throw new RestApiException(RestApiException.ATRIBUTO_INVALIDO_SCHEMA, voFieldName, getClass().getSimpleName());
        }

        // Não permite atributo duplicado no schema
        if (fieldsList.stream()
                .filter(f -> f.getVoFieldName().equals(voFieldName))
                .findAny()
                .isPresent()) {
            throw new RestApiException(RestApiException.ATRIBUTO_DUPLICADO_SCHEMA, voFieldName, getClass().getSimpleName());
        }

        RestFieldType fieldType = null;

        Object valueExpression = callback.execute();
        if (valueExpression instanceof Integer
            || valueExpression instanceof Long
            || valueExpression instanceof Short) {
            fieldType = RestFieldType.INTEGER;
        } else if (valueExpression instanceof Boolean) {
            fieldType = RestFieldType.BOOLEAN;
        } else if (valueExpression instanceof java.sql.Date ||
                   valueExpression instanceof java.util.Date) {
            fieldType = RestFieldType.DATE;
        } else if (valueExpression instanceof String) {
            fieldType = RestFieldType.STRING;
        }else{
            throw new RestApiException(RestApiException.TYPE_REST_FIELD_EXPRESSION_INVALID);
        }

        RestField field = new RestField(this, voFieldName, null, fieldType);
        field.__setValueExpression(valueExpression);
        IRestApiProvider apiProvider = contract.getApiProvider();
        fieldsList.add(field);
        return field;
    }

    @Override
    public Optional<RestField> getFieldByVoName(final String voFieldName) {
        String voFieldName2 = voFieldName != null && voFieldName.equals("pk") ? "id" : voFieldName;
        return fieldsList.stream().
                filter(f -> f.getVoFieldName().equals(voFieldName2)).
                findFirst();
    }

    @Override
    public RestField addFieldAsInteger(final String voFieldName, final String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, false);
    }

    @Override
    public RestField addFieldAsLong(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.LONG, null, false);
    }

    @Override
    public RestField addFieldAsLong(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.LONG, null, filterRequired);
    }

    @Override
    public RestField addFieldAsNonNegInteger(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, false)
                .setMinValue(0);
    }

    @Override
    public RestField addFieldAsNonNegInteger(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, filterRequired)
                .setMinValue(0);
    }

    @Override
    public RestField addFieldAsDouble(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.DOUBLE, null, false);
    }

    @Override
    public RestField addFieldAsDate(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.DATE, null, false);
    }

    @Override
    public RestField addFieldAsTime(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.TIME, null, false);
    }

    @Override
    public RestField addFieldAsString(String voFieldName, String fieldName, Integer fieldLength, boolean filterRequired, boolean autoTrim) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, filterRequired)
                .setAutoTrim(autoTrim);
    }

    @Override
    public RestField addFieldAsString(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, 100, false);
    }

    @Override
    public RestField addFieldAsString(String voFieldName, String fieldName, Integer fieldLength) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, false);
    }

    @Override
    public RestField addFieldAsUpperCaseString(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, 100, false)
                .setCharCase(RestFieldCharCase.UPPERCASE);
    }

    @Override
    public RestField addFieldAsUpperCaseString(String voFieldName, String fieldName, Integer fieldLength) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, false)
                .setCharCase(RestFieldCharCase.UPPERCASE);
    }

    @Override
    public RestField addFieldAsUpperCaseString(String voFieldName, String fieldName, Integer fieldLength, boolean filterRequired, boolean autoTrim) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, filterRequired)
                .setAutoTrim(autoTrim)
                .setCharCase(RestFieldCharCase.UPPERCASE);
    }

    @Override
    public RestField addFieldAsLowerCaseString(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, 100, false)
                .setCharCase(RestFieldCharCase.LOWERCASE);
    }

    @Override
    public RestField addFieldAsLowerCaseString(String voFieldName, String fieldName, Integer fieldLength) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, false)
                .setCharCase(RestFieldCharCase.LOWERCASE);
    }

    @Override
    public RestField addFieldAsLowerCaseString(String voFieldName, String fieldName, Integer fieldLength, boolean filterRequired, boolean autoTrim) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, fieldLength, filterRequired)
                .setAutoTrim(autoTrim)
                .setCharCase(RestFieldCharCase.LOWERCASE);

    }

    @Override
    public RestField addFieldAsInteger(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, filterRequired);
    }

    @Override
    public RestField addFieldAsDouble(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.DOUBLE, null, filterRequired);
    }

    @Override
    public RestField addFieldAsDate(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.DATE, null, filterRequired);
    }

    @Override
    public RestField addFieldAsTime(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.TIME, null, filterRequired);
    }

    @Override
    public RestField addFieldAsIdentify(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.LONG, null, false)
                .setFieldSubType(RestFieldSubType.IDENTITY)
                .setMinValue(RestField.MIN_IDENTITY_VALUE)
                .setMaxValue(RestField.MAX_IDENTITY_VALUE);
    }

    @Override
    public RestField addFieldAsIdentify(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.LONG, null, filterRequired)
                .setFieldSubType(RestFieldSubType.IDENTITY)
                .setMinValue(RestField.MIN_IDENTITY_VALUE)
                .setMaxValue(RestField.MAX_IDENTITY_VALUE);
    }

    @Override
    public RestField addFieldAsDay(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, false)
                .setFieldSubType(RestFieldSubType.DAY)
                .setMinValue(1)
                .setMaxValue(31);
    }

    @Override
    public RestField addFieldAsMonth(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, false)
                .setFieldSubType(RestFieldSubType.DAY)
                .setMinValue(1)
                .setMaxValue(12);
    }

    @Override
    public RestField addFieldAsYear(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, false)
                .setFieldSubType(RestFieldSubType.DAY)
                .setMinValue(1500)
                .setMaxValue(3000);
    }

    @Override
    public RestField addFieldAsEMail(String voFieldName, String fieldName) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, null, false)
                .setFieldSubType(RestFieldSubType.EMAIL);
    }

    @Override
    public RestField addFieldAsDay(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, filterRequired)
                .setFieldSubType(RestFieldSubType.DAY)
                .setMinValue(1)
                .setMaxValue(31);
    }

    @Override
    public RestField addFieldAsMonth(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, filterRequired)
                .setFieldSubType(RestFieldSubType.DAY)
                .setMinValue(1)
                .setMaxValue(12);
    }

    @Override
    public RestField addFieldAsYear(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.INTEGER, null, filterRequired)
                .setFieldSubType(RestFieldSubType.DAY)
                .setMinValue(1500)
                .setMaxValue(3000);
    }

    @Override
    public RestField addFieldAsEmail(String voFieldName, String fieldName, boolean filterRequired) {
        return createField(voFieldName, fieldName, RestFieldType.STRING, null, filterRequired)
                .setFieldSubType(RestFieldSubType.EMAIL);
    }

    @Override
    public IRestApiContract getContract() {
        return contract;
    }

    @Override
    public RestField addFieldAsExpression(final String voFieldName, final RestFieldExpressionCallback callback) {
        return createFieldExpression(voFieldName, callback);
    }

}
