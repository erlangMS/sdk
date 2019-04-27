/*
 * 
 */
package br.erlangms.rest.request;

import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.exception.RestApiException;
import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
public enum RestApiRequestConditionOperator implements Serializable{
    Equal,
    Contains,
    IContains,
    Like,
    ILike,
    NotContains,
    INotContains,
    NotLike,
    INotLike,
    GrantThen,
    GrantThenEgual,
    LessThen,
    LessThenEqual,
    NotEqual,
    IsNull,
    In;

    public static RestApiRequestConditionOperator fieldOperatorTokenToEnum(final String fieldOperatorToken) {
        switch (fieldOperatorToken) {
            case "contains":
                return RestApiRequestConditionOperator.Contains;
            case "icontains":
                return RestApiRequestConditionOperator.IContains;
            case "like":
                return RestApiRequestConditionOperator.Like;
            case "ilike":
                return RestApiRequestConditionOperator.ILike;
            case "notcontains":
                return RestApiRequestConditionOperator.NotContains;
            case "inotcontains":
                return RestApiRequestConditionOperator.IContains;
            case "notlike":
                return RestApiRequestConditionOperator.NotLike;
            case "inotlike":
                return RestApiRequestConditionOperator.INotLike;
            case "gt":
                return RestApiRequestConditionOperator.GrantThen;
            case "gte":
                return RestApiRequestConditionOperator.GrantThenEgual;
            case "lt":
                return RestApiRequestConditionOperator.LessThen;
            case "lte":
                return RestApiRequestConditionOperator.GrantThenEgual;
            case "e":
                return RestApiRequestConditionOperator.Equal;
            case "ne":
                return RestApiRequestConditionOperator.NotEqual;
            case "isnull":
                return RestApiRequestConditionOperator.IsNull;
            case "equal":
                return RestApiRequestConditionOperator.Equal;
            case "in":
                return RestApiRequestConditionOperator.In;
        }
        throw new RestApiConstraintException(String.format(RestApiConstraintException.OPERADOR_DE_ATRIBUTO_INVALIDO, fieldOperatorToken));
    }


    public static String fieldOperatorToSqlOperator(final RestApiRequestConditionOperator fieldOperator) {
        switch (fieldOperator) {
            case Contains:
                return " like ";
            case IContains:
                return " like ";
            case Like:
                return " like ";
            case ILike:
                return " like ";
            case NotContains:
                return " not like ";
            case INotContains:
                return " not like ";
            case NotLike:
                return " like ";
            case INotLike:
                return " not like ";
            case GrantThen:
                return " > ";
            case GrantThenEgual:
                return " >= ";
            case LessThen:
                return " < ";
            case LessThenEqual:
                return " <= ";
            case Equal:
                return " = ";
            case NotEqual:
                return " != ";
            case IsNull:
                return " is null ";
            case In:
                return " in ";
        }
        throw new RestApiException("Operador de atributo " + fieldOperator.toString() + " inválido.");
    }

    public static String enumToFieldOperatorToken(RestApiRequestConditionOperator fieldOperator) {
        switch (fieldOperator) {
            case Contains:
                return "contains";
            case IContains:
                return "icontains";
            case Like:
                return "like";
            case ILike:
                return "ilike";
            case NotContains:
                return "notcontains";
            case INotContains:
                return "inotcontains";
            case NotLike:
                return "notlike";
            case INotLike:
                return "inotlike";
            case GrantThen:
                return "gt";
            case GrantThenEgual:
                return "gte";
            case LessThen:
                return "lt";
            case LessThenEqual:
                return "lte";
            case Equal:
                return "e";
            case NotEqual:
                return "ne";
            case IsNull:
                return "isnull";
            case In:
                return "in";
        }
        throw new RestApiConstraintException("Operador de atributo " + fieldOperator.toString() + " inválido.");
    }

}
