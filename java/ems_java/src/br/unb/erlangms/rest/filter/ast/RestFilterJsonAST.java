package br.unb.erlangms.rest.filter.ast;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.filter.RestFilterCondition;
import br.unb.erlangms.rest.filter.tokens.RestFilterJsonToken;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.RestApiRequestConditionOperator;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.util.RestUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RestFilterJsonAST extends RestFilterAST {
    private final RestFilterJsonToken filterToken;
    private final Map<String, Object> filterMap;
    private final List<RestFilterCondition> conditions;

    public RestFilterJsonAST(RestFilterJsonToken filterToken, IRestApiProvider apiProvider) {
        this.conditions = new ArrayList<>();
        this.filterToken = filterToken;
        this.filterMap = parseJsonFilter(apiProvider);
    }

    public Map<String, Object> getFilterMap() {
        return filterMap;
    }

    public List<RestFilterCondition> getFilter() {
        return conditions;
    }

    /**
     * Método responsável por fazer o parser de um jsonFilter
     *
     * @author Everton de Vargas Agilar 
     * @version 1.0.0
     * @param filter
     * @param apiProvider
     * @return
     * @since 21/03/2019
     *
     */
    private Map<String, Object> parseJsonFilter(final IRestApiProvider apiProvider) {
        String filter = filterToken.getValue();
        if (filter != null) {
            filter = RestUtils.unquoteString(filter);
            if (!filter.isEmpty() && !filter.equals("{}")) {
                Map<String, Object> result;
                try {
                    result = RestUtils.fromJson(filter, HashMap.class);
                } catch (Exception ex) {
                    throw new RestApiException("Sintáxe json inválida: " + filter);
                }
                if (!result.isEmpty()) {
                    Class jsonClass = apiProvider.getVoClass();
                    int parameterNameOffset = filterToken.getPosition();
                    for (String condition : result.keySet()) {
                        String[] field_defs = condition.split("__");
                        String voFieldName;
                        String operatorToken;
                        RestApiRequestConditionOperator operator;
                        String parameterName;
                        RestField field;
                        int field_len = field_defs.length;
                        switch (field_len) {
                            case 1:
                                voFieldName = condition;
                                operator = RestApiRequestConditionOperator.Equal;
                                break;
                            case 2:
                                voFieldName = field_defs[0];
                                operatorToken = field_defs[1];
                                operator = RestApiRequestConditionOperator.fieldOperatorTokenToEnum(operatorToken);
                                break;
                            default:
                                throw new RestApiException("Atributo " + condition + " não existe.");
                        }

                        apiProvider.getContract().checkSupportFieldOperator(operator);

                        Optional<RestField> fieldOptional = apiProvider.getContract()
                                .getSchema()
                                .getFieldByVoName(voFieldName);

                        if (fieldOptional.isPresent()) {
                            field = fieldOptional.get();
                        } else {
                            throw new RestApiException(String.format(RestApiException.ATRIBUTO_IN_OPERADOR_FILTER_NAO_EXISTE, voFieldName));
                        }

                        // Computa um nome de parâmetro único para o operador de atributo
                        // Obs.: Somente isnull não precisa
                        if (operator != RestApiRequestConditionOperator.IsNull) {
                            parameterName = "pRestApi" + Integer.toHexString(parameterNameOffset++);
                        } else {
                            parameterName = null;
                        }

                        Object fieldValue = result.get(condition);

                        if (operator == RestApiRequestConditionOperator.IsNull) {
                            fieldValue = field.parseValueAsBoolean(fieldValue);
                        } else {
                            fieldValue = RestField.parseValue(field, fieldValue, true);
                        }
                        RestFilterCondition filterCondition = new RestFilterCondition(field,
                                                                                      operator,
                                                                                      parameterName,
                                                                                      fieldValue);
                        conditions.add(filterCondition);
                    }
                    return result;
                }
                throw new RestApiException(String.format(RestApiException.OPERATOR_FILTER_INVALID, filter));
            }
        }
        throw new RestApiException(String.format(RestApiException.OPERATOR_FILTER_INVALID, filter));
    }

}
