package br.unb.erlangms.rest.schema;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.provider.RestFieldValueSerializeCallback;
import br.unb.erlangms.rest.util.RestUtils;
import br.unb.erlangms.rest.util.StringEnum;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Classe que representa um campo de um objeto.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 27/03/2019
 *
 */
public class RestField implements Serializable {
    private static final long serialVersionUID = -1554944779502407806L;
    public static final Long MIN_IDENTITY_VALUE = 1L;
    public static final Long MAX_IDENTITY_VALUE = 999999999999L;
    private final String voFieldName;
    private final String fieldName;
    private final RestFieldType fieldType;
    private RestFieldSubType fieldSubType = null;
    private boolean primaryKey = false;
    private boolean autoTrim = true;            // s� para strings
    private RestFieldCharCase charCase = null;  // s� para strings
    private Integer fieldLength = null;         // s� para strings
    private Integer minLength = null;           // s� para strings
    private boolean filterRequired = false;
    private RestFieldSortType sortType = RestFieldSortType.ASC;
    private Field attrBase = null;
    private Field attr = null;
    private String attrBaseRelationName = null;
    private Object defaultValue = null;
    private Object maxValue = null;
    private Object minValue = null;
    private String dateFormat = null;           // s� para date
    private final IRestApiSchema schema;
    private final IRestApiProvider apiProvider;
    private Object valueExpression = null;
    private RestJoinType joinType = RestJoinType.LEFT_JOIN;

    public RestField(IRestApiSchema schema, String voFieldName, String fieldName, RestFieldType fieldType) {
        this.schema = schema;
        this.voFieldName = voFieldName;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.apiProvider = schema.getContract().getApiProvider();
    }

    public String getVoFieldName() {
        return voFieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public RestFieldType getFieldType() {
        return fieldType;
    }

    public RestFieldSubType getFieldSubType() {
        return fieldSubType;
    }

    public RestField setFieldSubType(RestFieldSubType fieldSubType) {
        this.fieldSubType = fieldSubType;
        return this;
    }

    public Integer getFieldLength() {
        return fieldLength;
    }

    public RestField setFieldLength(Integer length) {
        this.fieldLength = length;
        if (fieldLength != null && (fieldLength < 0 || fieldLength > 9999)) {
            throw new RestApiException(RestApiException.INVALID_FIELD_LENGTH_SCHEMA, voFieldName);
        }
        return this;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
        if (minLength != null) {
            if (fieldLength != null && minLength > fieldLength) {
                throw new RestApiException(RestApiException.MIN_LENGTH_GT_FIELD_LENGTH_SCHEMA, voFieldName);
            }
            if (minLength < 0 || minLength > 9999) {
                throw new RestApiException(RestApiException.INVALID_MIN_LENGTH_SCHEMA, voFieldName);
            }
        }
    }

    public boolean isAttributeObject() {
        return attrBase != null;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public RestField setIsPrimaryKey(boolean isPrimaryKey) {
        this.primaryKey = isPrimaryKey;
        return this;
    }

    public RestField setSortType(RestFieldSortType sortType) {
        this.sortType = sortType;
        return this;
    }

    public RestFieldSortType getSortType() {
        return sortType;
    }

    public RestField setAttrBase(Field attrBase) {
        this.attrBase = attrBase;
        return this;
    }

    public Field getAttrBase() {
        return attrBase;
    }

    public Field getAttr() {
        return attr;
    }

    public RestField setAttr(Field attr) {
        this.attr = attr;
        return this;
    }

    public String getAttrBaseRelationName() {
        return attrBaseRelationName;
    }

    public void setAttrBaseRelationName(String attrBaseRelationName) {
        this.attrBaseRelationName = attrBaseRelationName;
    }

    public boolean isAutoTrim() {
        return autoTrim;
    }

    public RestField setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
        return this;
    }

    public RestFieldCharCase getCharCase() {
        return charCase;
    }

    public RestField setCharCase(RestFieldCharCase charCase) {
        this.charCase = charCase;
        return this;
    }

    public RestField setFilterRequired(boolean filterRequired) {
        this.filterRequired = filterRequired;
        return this;
    }

    public boolean isFilterRequired() {
        return filterRequired;
    }

    public Object getMaxValue() {
        return maxValue;
    }

    public RestField setMaxValue(Object maxValue) {
        if (fieldType == RestFieldType.INTEGER && !(maxValue instanceof Integer)) {
            this.maxValue = parseValueAsInteger(maxValue, false);
        } else if (fieldType == RestFieldType.LONG && !(maxValue instanceof Long)) {
            this.maxValue = parseValueAsLong(maxValue, false);
        } else if (fieldType == RestFieldType.DOUBLE && !(maxValue instanceof Double)) {
            this.maxValue = parseValueAsDouble(maxValue, false);
        } else {
            this.maxValue = maxValue;
        }
        return this;
    }

    public Object getMinValue() {
        return minValue;
    }

    public RestField setMinValue(Object minValue) {
        if (fieldType == RestFieldType.INTEGER && !(minValue instanceof Integer)) {
            this.minValue = parseValueAsInteger(minValue, false);
        } else if (fieldType == RestFieldType.LONG && !(minValue instanceof Long)) {
            this.minValue = parseValueAsLong(minValue, false);
        } else if (fieldType == RestFieldType.DOUBLE && !(minValue instanceof Double)) {
            this.minValue = parseValueAsDouble(minValue, false);
        } else {
            this.minValue = minValue;
        }
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public RestField setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public RestField setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public Object parseValueAsInteger(final Object fieldValue, boolean allowValidate) {
        Object result = fieldValue;
        if (fieldValue != null) {
            try {
                if (fieldValue instanceof Integer) {
                    // ok
                } else if (fieldValue instanceof String) {
                    try {
                        result = Integer.parseInt((String) fieldValue);
                    } catch (NumberFormatException ex) {
                        // tenta ver se consegue converter fazendo trim
                        result = Integer.parseInt(((String) fieldValue).trim());
                    }
                } else if (fieldValue instanceof Double) {
                    result = ((Double) fieldValue).intValue();
                } else if (fieldValue instanceof Long) {
                    result = ((Long) fieldValue).intValue();
                } else if (fieldValue instanceof ArrayList<?>) {
                    //Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
                    List<Integer> value_field_parameter = new ArrayList<>();
                    if (((ArrayList) fieldValue).size() > 0) {
                        //Tests the type of the array using the first position
                        if (((ArrayList) fieldValue).get(0) instanceof String) {
                            for (String string : (ArrayList<String>) fieldValue) {
                                value_field_parameter.add(Integer.parseInt(string));
                            }
                        } else if (((ArrayList) fieldValue).get(0) instanceof Double) {
                            for (Double doubleValue : (ArrayList<Double>) fieldValue) {
                                value_field_parameter.add(doubleValue.intValue());
                            }
                        }
                    }
                    result = value_field_parameter;
                } else if (fieldValue instanceof Short) {
                    result = ((Short) fieldValue).intValue();
                } else if (fieldValue instanceof BigInteger) {
                    result = ((BigInteger) fieldValue).intValue();
                } else if (fieldValue instanceof BigDecimal) {
                    result = ((BigDecimal) fieldValue).intValue();
                } else if (fieldValue instanceof Boolean) {
                    result = ((Boolean) fieldValue) ? 1 : 0;
                } else {
                    throw new RestApiException(RestApiException.VALOR_ATRIBUTO_INTEGER_INVALIDO, fieldValue, getVoFieldName());
                }
            } catch (RestApiException ex) {
                throw ex;
            } catch (NumberFormatException ex) {
                throw new RestApiException(RestApiException.VALOR_ATRIBUTO_INTEGER_INVALIDO, fieldValue, getVoFieldName());
            }

            if (allowValidate && result instanceof Integer) {
                if (getMaxValue() != null && (Integer) result > (Integer) getMaxValue()) {
                    throw new RestApiException(RestApiException.VALOR_MAX_ATRIBUTO_FORA_INTERVALO, result, getVoFieldName(), getMaxValue());
                }
                if (getMinValue() != null && (Integer) result < (Integer) getMinValue()) {
                    throw new RestApiException(RestApiException.VALOR_MIN_ATRIBUTO_FORA_INTERVALO, result, getVoFieldName(), getMinValue());
                }
            }

            RestFieldValueSerializeCallback fieldValueSerializeCallback = apiProvider.getFieldValueSerializeAsIntegerCallback();
            if (fieldValueSerializeCallback != null) {
                result = fieldValueSerializeCallback.execute(this, result);
            }
        } else {
            if (defaultValue != null) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * Parse um objeto String, Double ou Float em um valor Double.
     *
     * @param fieldValue    	valor String, Double ou Float.
     * @param allowValidate		indica se permite validar durante o parser
     * @return Double ou null
     * @author Everton de Vargas Agilar (revisão)
     */
    public Object parseValueAsDouble(final Object fieldValue, boolean allowValidate) {
        Object result = fieldValue;
        if (fieldValue != null) {
            try {
                if (fieldValue instanceof Double) {
                    // ok
                } else if (fieldValue instanceof String) {
                    result = Double.parseDouble((String) fieldValue);
                } else if (fieldValue instanceof Long) {
                    result = ((Long) fieldValue).doubleValue();
                } else if (fieldValue instanceof Integer) {
                    result = ((Integer) fieldValue).doubleValue();
                } else if (fieldValue instanceof ArrayList<?>) {
                    //Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
                    List<Double> value_field_parameter = new ArrayList<>();
                    if (((ArrayList) fieldValue).size() > 0) {
                        //Tests the type of the array using the first position
                        if (((ArrayList) fieldValue).get(0) instanceof String) {
                            for (String string : (ArrayList<String>) fieldValue) {
                                value_field_parameter.add(Double.valueOf(string));
                            }
                        } else if (((ArrayList) fieldValue).get(0) instanceof Double) {
                            for (Double doubleValue : (ArrayList<Double>) fieldValue) {
                                value_field_parameter.add(doubleValue);
                            }
                        }
                    }
                    result = value_field_parameter;
                } else if (fieldValue instanceof BigDecimal) {
                    result = ((BigDecimal) fieldValue).doubleValue();
                } else if (fieldValue instanceof Short) {
                    result = ((Short) fieldValue).doubleValue();
                } else if (fieldValue instanceof BigInteger) {
                    result = ((BigInteger) fieldValue).doubleValue();
                } else {
                    throw new RestApiException(RestApiException.VALOR_ATRIBUTO_DOUBLE_INVALIDO, fieldValue, getVoFieldName());
                }
            } catch (RestApiException ex) {
                throw ex;
            } catch (NumberFormatException ex) {
                throw new RestApiException(RestApiException.VALOR_ATRIBUTO_DOUBLE_INVALIDO, fieldValue, getVoFieldName());
            }

            if (allowValidate && result instanceof Double) {
                if (getMaxValue() != null && getMaxValue() instanceof Double && (Double) result > (Double) getMaxValue()) {
                    throw new RestApiException(RestApiException.VALOR_MAX_ATRIBUTO_FORA_INTERVALO, result, getVoFieldName(), getMaxValue());
                }
                if (getMinValue() != null && getMinValue() instanceof Double && (Double) result < (Double) getMinValue()) {
                    throw new RestApiException(RestApiException.VALOR_MIN_ATRIBUTO_FORA_INTERVALO, result, getVoFieldName(), getMinValue());
                }
            }

            RestFieldValueSerializeCallback fieldValueSerializeCallback = apiProvider.getFieldValueSerializeAsDoubleCallback();
            if (fieldValueSerializeCallback != null) {
                result = fieldValueSerializeCallback.execute(this, result);
            }
        } else {
            if (defaultValue != null) {
                result = defaultValue;
            }
        }
        return result;
    }

    public Object parseValueAsLong(final Object fieldValue, boolean allowValidate) {
        Object result = fieldValue;
        if (fieldValue != null) {
            try {
                if (fieldValue instanceof Long) {
                    // ok
                } else if (fieldValue instanceof String) {
                    result = Long.parseLong((String) fieldValue);
                } else if (fieldValue instanceof Integer) {
                    result = ((Integer) fieldValue).longValue();
                } else if (fieldValue instanceof Double) {
                    result = ((Double) fieldValue).longValue();
                } else if (fieldValue instanceof ArrayList<?>) {
                    //Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
                    List<Long> value_field_parameter = new ArrayList<>();
                    if (((ArrayList) fieldValue).size() > 0) {
                        //Tests the type of the array using the first position
                        if (((ArrayList) fieldValue).get(0) instanceof String) {
                            for (String string : (ArrayList<String>) fieldValue) {
                                value_field_parameter.add(Long.valueOf(string));
                            }
                        } else if (((ArrayList) fieldValue).get(0) instanceof Double) {
                            for (Double doubleValue : (ArrayList<Double>) fieldValue) {
                                value_field_parameter.add(doubleValue.longValue());
                            }
                        }
                    }
                    result = value_field_parameter;
                } else if (fieldValue instanceof BigDecimal) {
                    result = ((BigDecimal) fieldValue).longValue();
                } else if (fieldValue instanceof Short) {
                    result = ((Short) fieldValue).longValue();
                } else if (fieldValue instanceof BigInteger) {
                    result = ((BigInteger) fieldValue).longValue();
                } else {
                    throw new RestApiException(RestApiException.VALOR_ATRIBUTO_LONG_INVALIDO, fieldValue, getVoFieldName());
                }
            } catch (RestApiException ex) {
                throw ex;
            } catch (NumberFormatException ex) {
                throw new RestApiException(RestApiException.VALOR_ATRIBUTO_LONG_INVALIDO, fieldValue, getVoFieldName());
            }

            if (allowValidate && result instanceof Long) {
                if (getMaxValue() != null && getMaxValue() instanceof Long && (Long) result > (Long) getMaxValue()) {
                    throw new RestApiException(RestApiException.VALOR_MAX_ATRIBUTO_FORA_INTERVALO, result, getVoFieldName(), getMaxValue());
                }
                if (getMinValue() != null && getMinValue() instanceof Long && (Long) result < (Long) getMinValue()) {
                    throw new RestApiException(RestApiException.VALOR_MIN_ATRIBUTO_FORA_INTERVALO, result, getVoFieldName(), getMinValue(), getMaxValue());
                }
            }

            RestFieldValueSerializeCallback fieldValueSerializeCallback = apiProvider.getFieldValueSerializeAsLongCallback();
            if (fieldValueSerializeCallback != null) {
                result = fieldValueSerializeCallback.execute(this, result);
            }
        } else {
            if (defaultValue != null) {
                result = defaultValue;
            }
        }
        return result;
    }

    public Object parseAsString(Object fieldValue, boolean allowValidate) {
        Object result = fieldValue;
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                // ok
            } else if (fieldValue instanceof StringEnum) {
                result = ((StringEnum) fieldValue).getValue();
            } else if (fieldValue instanceof java.sql.Date) {
                result = parseValueAsDate(fieldValue, allowValidate);
            } else {
                result = fieldValue.toString();
            }

            if (autoTrim) {
                result = ((String) result).trim();
            }

            if (charCase != null) {
                switch (charCase) {
                    case LOWERCASE:
                        result = ((String) result).toLowerCase();
                        break;
                    case UPPERCASE:
                        result = ((String) result).toUpperCase();
                        break;
                }
            }

            if (allowValidate && minLength != null && ((String) result).length() < minLength) {
                throw new RestApiException(String.format(RestApiException.INVALID_MIN_LENGTH_ATRIBUTO, getVoFieldName(), minLength));
            }

            RestFieldValueSerializeCallback fieldValueSerializeCallback = apiProvider.getFieldValueSerializeAsStringCallback();
            if (fieldValueSerializeCallback != null) {
                result = fieldValueSerializeCallback.execute(this, result);
            }
        } else {
            if (defaultValue != null) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * Parse um objeto String, Double ou Boolean em um valor boolean.
     *
     * @param fieldValue valor String, Double ou Boolean.
     * @return boolean
     * @author Everton de Vargas Agilar (revisão)
     */
    public Object parseValueAsBoolean(final Object fieldValue) {
        Object result = false;
        if (fieldValue != null) {
            if (fieldValue instanceof Boolean) {
                result = fieldValue;
            } else if (fieldValue instanceof String) {
                if (((String) fieldValue).equalsIgnoreCase("T")) {
                    result = true;
                } else if (((String) fieldValue).equalsIgnoreCase("Y")) {
                    result = true;
                } else if (((String) fieldValue).equalsIgnoreCase("true")) {
                    result = true;
                } else if (((String) fieldValue).equals("1")) {
                    result = true;
                } else if (((String) fieldValue).equalsIgnoreCase("S")) {
                    result = true;
                } else if (((String) fieldValue).equals("1.0")) {
                    result = true;
                } else if (((String) fieldValue).equalsIgnoreCase("yes")) {
                    result = true;
                } else if (((String) fieldValue).equalsIgnoreCase("sim")) {
                    result = true;
                }
            } else if (fieldValue instanceof Double) {
                result = fieldValue.toString().equals("1.0");
            } else if (fieldValue.toString().equals("1")) {
                result = true;
            }

            RestFieldValueSerializeCallback fieldValueSerializeCallback = apiProvider.getFieldValueSerializeAsBooleanCallback();
            if (fieldValueSerializeCallback != null) {
                result = fieldValueSerializeCallback.execute(this, result);
            }
        } else {
            if (defaultValue != null) {
                result = defaultValue;
            }
        }
        return result;
    }

    public Object parseValueAsDate(final Object fieldValue, boolean allowValidate) {
        Object result = fieldValue;
        if (fieldValue != null) {
            String dateFormatSelected = dateFormat;

            if (fieldValue instanceof String) {
                if (dateFormatSelected == null || dateFormatSelected.isEmpty()) {
                    int len_value = ((String) fieldValue).length();
                    if (len_value >= 6 && len_value <= 10) {
                        dateFormatSelected = RestUtils.dateFormatDDMMYYYY;
                    } else if (len_value == 16) {
                        dateFormatSelected = RestUtils.dateFormatDDMMYYYY_HHmm;
                    } else if (len_value == 19) {
                        dateFormatSelected = RestUtils.dateFormatDDMMYYYY_HHmmss;
                    }
                }

                try {
                    if (dateFormatSelected == null || dateFormatSelected.isEmpty()) {
                        dateFormatSelected = RestUtils.dateFormatDDMMYYYY;
                    }
                    result = FastDateFormat.getInstance(dateFormatSelected).parseObject((String) fieldValue);
                } catch (ParseException ex) {
                    throw new RestApiException(RestApiException.VALOR_ATRIBUTO_DATE_INVALIDO, fieldValue, getVoFieldName(), dateFormatSelected);
                }
            } else if (fieldValue instanceof java.util.Date || fieldValue instanceof java.sql.Date) {
                if (dateFormatSelected == null || dateFormatSelected.isEmpty()) {
                    dateFormatSelected = RestUtils.dateFormatDDMMYYYY;
                }
                result = FastDateFormat.getInstance(dateFormatSelected).format(fieldValue);
            } else {
                throw new RestApiException(RestApiException.VALOR_ATRIBUTO_DATE_INVALIDO, fieldValue, getVoFieldName(), dateFormatSelected);
            }

            RestFieldValueSerializeCallback fieldValueSerializeCallback = apiProvider.getFieldValueSerializeAsDateCallback();
            if (fieldValueSerializeCallback != null) {
                result = fieldValueSerializeCallback.execute(this, result);
            }
        } else {
            if (defaultValue != null) {
                result = defaultValue;
            }
        }
        return result;
    }

    public static Object parseValue(final RestField field, final Object value, boolean allowValidate) {
        Object valueFormatted;
        switch (field.getFieldType()) {
            case STRING:
                valueFormatted = field.parseAsString(value, allowValidate);
                break;
            case INTEGER:
                valueFormatted = field.parseValueAsInteger(value, allowValidate);
                break;
            case DOUBLE:
                valueFormatted = field.parseValueAsDouble(value, allowValidate);
                break;
            case LONG:
                valueFormatted = field.parseValueAsLong(value, allowValidate);
                break;
            case BOOLEAN:
                valueFormatted = field.parseValueAsBoolean(value);
                break;
            case DATE:
                valueFormatted = field.parseValueAsDate(value, allowValidate);
                break;
            default:
                valueFormatted = value;
                break;
        }
        return valueFormatted;
    }

    public IRestApiSchema getSchema() {
        return schema;
    }

    public void __setValueExpression(Object valueExpression) {
        this.valueExpression = valueExpression;
    }

    public Object getValueExpression() {
        return valueExpression;
    }

    public RestJoinType getJoinType() {
        return joinType;
    }

    public RestField setJoinType(RestJoinType joinType) {
        this.joinType = joinType;
        return this;
    }



}
