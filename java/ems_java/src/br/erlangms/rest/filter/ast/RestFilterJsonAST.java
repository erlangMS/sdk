package br.erlangms.rest.filter.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.exception.RestApiException;
import br.erlangms.rest.filter.RestFilterCondition;
import br.erlangms.rest.filter.tokens.RestFilterJsonToken;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.schema.RestField;
import br.erlangms.rest.util.RestUtils;

public class RestFilterJsonAST extends RestFilterAST {
	private static final long serialVersionUID = 1523179326643700748L;
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
     * MÃ©todo responsável por fazer o parser de um jsonFilter
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
                    throw new RestApiException("SintÃ¡xe json inválida: " + filter);
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
                        if (field_len == 1) {
                            voFieldName = condition;
                            operatorToken = "=";
                            operator = RestApiRequestConditionOperator.Equal;
                        } else if (field_len == 2) {
                            voFieldName = field_defs[0];
                            operatorToken = field_defs[1];
                            operator = RestApiRequestConditionOperator.fieldOperatorTokenToEnum(operatorToken);
                        } else {
                            throw new RestApiException("Atributo " + condition + " não existe.");
                        }

                        apiProvider.getContract().checkSupportFieldOperator(operator);

                        Optional<RestField> fieldOptional = apiProvider.getContract()
                                .getSchema()
                                .getFieldByVoName(voFieldName);

                        if (fieldOptional.isPresent()) {
                            field = fieldOptional.get();
                        } else {
                            throw new RestApiConstraintException(String.format(RestApiConstraintException.ATRIBUTO_IN_OPERADOR_FILTER_NAO_EXISTE, voFieldName));
                        }

                        // Computa um nome de parÃ¢metro Ãºnico para o operador de atributo
                        // Obs.: Somente isnull não precisa
                        if (operator != RestApiRequestConditionOperator.IsNull) {
                            parameterName = "pRestApi" + Integer.toHexString(parameterNameOffset++);
                        } else {
                            parameterName = null;
                        }

                        Object fieldValue = result.get(condition);

                        RestFilterCondition filterCondition = new RestFilterCondition(field,
                                                                                      operator,
                                                                                      parameterName,
                                                                                      fieldValue);
                        conditions.add(filterCondition);
                    }
                    return result;
                }
                throw new RestApiException(String.format(RestApiConstraintException.OPERATOR_FILTER_INVALID, filter));
            }
        }
        throw new RestApiException(String.format(RestApiConstraintException.OPERATOR_FILTER_INVALID, filter));
    }

}
