/*
 * 
 */
package br.erlangms.rest.filter;

import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.schema.RestField;
import java.io.Serializable;
import java.util.Objects;

/**
 * Representa uma condição do operador filter.
 *
 * @author evertonagilar
 */
public class RestFilterCondition implements Serializable {

    private final RestField field;
    private final RestApiRequestConditionOperator operator;
    private final String parameterName;
    private Object value;

    // Gerado durante a etapa de geraÃ§Ã£o de código
    private String sqlOperator;
    private String sqlFieldName;

    public RestFilterCondition(RestField field, RestApiRequestConditionOperator fieldOperator, String parameterName, Object fieldValue) {
        this.field = field;
        this.operator = fieldOperator;
        this.parameterName = parameterName;
        this.value = fieldValue;
        this.sqlFieldName = null;
        this.sqlOperator = null;
    }

    public RestField getField() {
        return field;
    }

    public RestApiRequestConditionOperator getOperator() {
        return operator;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object fieldValue) {
        this.value = fieldValue;
    }

    public String getSqlFieldName() {
        return sqlFieldName;
    }

    public void setSqlFieldName(String sqlFieldName) {
        this.sqlFieldName = sqlFieldName;
    }

    public String getSqlOperator() {
        return sqlOperator;
    }

    public void setSqlOperator(String sqlOperator) {
        this.sqlOperator = sqlOperator;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.field);
        hash = 67 * hash + Objects.hashCode(this.operator);
        hash = 67 * hash + Objects.hashCode(this.sqlOperator);
        hash = 67 * hash + Objects.hashCode(this.parameterName);
        hash = 67 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RestFilterCondition other = (RestFilterCondition) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        if (this.operator != other.operator) {
            return false;
        }
        if (!Objects.equals(this.sqlOperator, other.sqlOperator)) {
            return false;
        }
        if (!Objects.equals(this.parameterName, other.parameterName)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }



}
