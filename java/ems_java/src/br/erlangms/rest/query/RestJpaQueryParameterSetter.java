/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.exception.RestApiException;
import br.erlangms.rest.filter.IRestFilterASTVisitor;
import br.erlangms.rest.filter.RestFilterCondition;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.util.EnumUtils;
import static br.erlangms.rest.util.RestUtils.parseAsBoolean;
import static br.erlangms.rest.util.RestUtils.parseAsDouble;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Parameter;
import javax.persistence.Query;

/**
 * Classe responsável por setar os parâmetros de uma query parametrizada
 * a partir dos dados contidos em uma AST do filter
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 28/03/2019
 *
 */
public class RestJpaQueryParameterSetter implements IRestFilterASTVisitor {
    private final Query query;
    private final IRestApiRequest request;
    private final RestJpaParameterQueryCallback parameterQueryTransform;

    public RestJpaQueryParameterSetter(IRestApiRequest request, Query query) {
        this.query = query;
        this.request = request;
        this.parameterQueryTransform = request.getApiProvider().getParameterQueryCallback();
    }

    @Override
    public Object accept(final RestFilterAST ast) {
        if (ast instanceof RestFilterJsonAST) {
            RestFilterJsonAST filterJsonAST = (RestFilterJsonAST) ast;
            setQueryParameters(filterJsonAST.getFilter());
        }
        return query;
    }

    public Query getQuery() {
        return query;
    }

    /**
     * Seta os valores nos parâmetros de um query a partir de um map de filtros
     *
     * @param filterMap	map com chave/valor dos dados que serÃ£o aplicados na query
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setQueryParameters(final List<RestFilterCondition> filterConditions) {
        if (query != null && !filterConditions.isEmpty()) {
            SimpleDateFormat dateFormatDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat dateFormatDDMMYYYY_HHmm = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            SimpleDateFormat dateFormatDDMMYYYY_HHmmss = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            for (RestFilterCondition condition : filterConditions) {
                try {

                    // Condições isnull não tem parâmetros para setar
                    if (condition.getOperator() == RestApiRequestConditionOperator.IsNull){
                        continue;
                    }

                    Parameter parameter;
                    try{
                        parameter = query.getParameter(condition.getParameterName());
                    }catch (Exception e){
                        // Não achou o parameter então é um parrameter da viewSql
                        // Nesse caso, apenas invoca parameterQueryTransform
                        parameterQueryTransform(condition);
                        continue;
                    }

                    Class<?> paramType = parameter.getParameterType();
                    Object fieldValue = condition.getValue();

                    if (paramType == null) {
                        if (fieldValue instanceof ArrayList<?>) {
                            paramType = ((ArrayList<?>) fieldValue).get(0).getClass();
                        } else {
                            paramType = fieldValue.getClass();
                        }
                    }
                    if (paramType == Integer.class) {
                        if (fieldValue instanceof String) {
                            query.setParameter(condition.getParameterName(), Integer.parseInt((String) fieldValue));
                        } else if (fieldValue instanceof Double) {
                            query.setParameter(condition.getParameterName(), ((Double) fieldValue).intValue());
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
                            query.setParameter(condition.getParameterName(), value_field_parameter);
                        } else {
                            query.setParameter(condition.getParameterName(), fieldValue);
                        }
                    } else if (paramType == BigDecimal.class) {
                        if (fieldValue instanceof String) {
                            query.setParameter(condition.getParameterName(), BigDecimal.valueOf(Double.parseDouble((String) fieldValue)));
                        } else {
                            query.setParameter(condition.getParameterName(), BigDecimal.valueOf((double) fieldValue));
                        }
                    } else if (paramType == Double.class || paramType == double.class) {
                        if (fieldValue instanceof ArrayList<?>) {
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
                            query.setParameter(condition.getParameterName(), value_field_parameter);
                        } else {
                            double valueDouble = parseAsDouble(fieldValue);
                            query.setParameter(condition.getParameterName(), valueDouble);
                        }
                    } else if (paramType == Long.class || paramType == long.class) {
                        if (fieldValue instanceof ArrayList<?>) {
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
                            query.setParameter(condition.getParameterName(), value_field_parameter);
                        } else {
                            Double valueDouble = parseAsDouble(fieldValue);
                            query.setParameter(condition.getParameterName(), valueDouble.longValue());
                        }

                    } else if (paramType.isEnum()) {

                        Integer idValue;
                        Enum<?> valueEnum;
                        if (fieldValue instanceof String) {
                            try {
                                idValue = Integer.parseInt((String) fieldValue);
                                valueEnum = EnumUtils.intToEnum(idValue, (Class<Enum>) paramType);
                            } catch (NumberFormatException e) {
                                valueEnum = EnumUtils.strToEnum((String) fieldValue, (Class<Enum>) paramType);
                            }
                        } else {
                            idValue = ((Double) fieldValue).intValue();
                            valueEnum = EnumUtils.intToEnum(idValue, (Class<Enum>) paramType);
                        }

                        query.setParameter(condition.getParameterName(), valueEnum);

                    } else if (paramType == String.class) {
                        String fieldValueAsString;
                        if (fieldValue instanceof Double) {
                            // Parece um inteiro? (termina com .0)
                            if (fieldValue.toString().endsWith(".0")) {
                                fieldValueAsString = Integer.toString(((Double) fieldValue).intValue());
                            } else {
                                fieldValueAsString = fieldValue.toString();
                            }
                        } else {
                            fieldValueAsString = fieldValue.toString();
                        }

                        // Muito cuidado com valueString == "" para não gerar um sql like %%
                        if (!fieldValueAsString.isEmpty()) {
                            switch (condition.getOperator()) {
                                case Contains:
                                    fieldValueAsString = "%" + fieldValueAsString + "%";
                                    break;
                                case IContains:
                                    fieldValueAsString = "%" + fieldValueAsString.toLowerCase() + "%";
                                    break;
                                case Like:
                                    fieldValueAsString = fieldValueAsString + "%";
                                    break;
                                case ILike:
                                    fieldValueAsString = fieldValueAsString.toLowerCase() + "%";
                                    break;
                                case NotContains:
                                    fieldValueAsString = "%" + fieldValueAsString + "%";
                                    break;
                                case INotContains:
                                    fieldValueAsString = "%" + fieldValueAsString.toLowerCase() + "%";
                                    break;
                                case NotLike:
                                    fieldValueAsString = fieldValueAsString + "%";
                                    break;
                                case INotLike:
                                    fieldValueAsString = fieldValueAsString.toLowerCase() + "%";
                                    break;
                            }
                        }

                        condition.setValue(fieldValueAsString);
                        fieldValueAsString = (String) parameterQueryTransform(condition);
                        query.setParameter(condition.getParameterName(), fieldValueAsString);
                    } else if (paramType == Boolean.class) {
                        boolean value_boolean = parseAsBoolean(fieldValue);
                        query.setParameter(condition.getParameterName(), value_boolean);
                    } else if (paramType == java.util.Date.class) {
                        final String m_erro = condition.getField().getVoFieldName() + " não Ã© uma data válida.";
                        if (fieldValue instanceof String) {
                            int len_value = ((String) fieldValue).length();
                            try {
                                if (len_value >= 6 && len_value <= 10) {
                                    query.setParameter(condition.getParameterName(), dateFormatDDMMYYYY.parse((String) fieldValue));
                                } else if (len_value == 16) {
                                    query.setParameter(condition.getParameterName(), dateFormatDDMMYYYY_HHmm.parse((String) fieldValue));
                                } else if (len_value == 19) {
                                    query.setParameter(condition.getParameterName(), dateFormatDDMMYYYY_HHmmss.parse((String) fieldValue));
                                } else {
                                    throw new RestApiException(m_erro);
                                }
                            } catch (ParseException e) {
                                throw new RestApiException(m_erro);
                            }
                        } else {
                            throw new RestApiException(m_erro);
                        }
                    } else {
                        query.setParameter(condition.getParameterName(), fieldValue);
                    }
                } catch (RestApiException | NumberFormatException e) {
                    throw new RestApiException("Erro ao setar parâmetros da query. Motivo: " + e.getMessage());
                }
            }
        }
    }

    private Object parameterQueryTransform(final RestFilterCondition condition) {
        if (parameterQueryTransform != null) {
            return parameterQueryTransform.execute(condition, query);
        }
        return condition.getValue();
    }

}
