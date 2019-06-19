package br.unb.erlangms.rest.query;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.filter.IRestFilterASTVisitor;
import br.unb.erlangms.rest.filter.RestFilterCondition;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import br.unb.erlangms.rest.request.RestApiRequestConditionOperator;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.util.EnumUtils;
import static br.unb.erlangms.rest.util.RestUtils.parseAsBoolean;
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
    private final IRestApiRequestInternal request;
    private final RestJpaParameterQueryCallback parameterQueryTransform;

    public RestJpaQueryParameterSetter(IRestApiRequestInternal request, Query query) {
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
     * @param filterMap	map com chave/valor dos dados que serão aplicados na query
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
                    if (condition.getOperator() == RestApiRequestConditionOperator.IsNull) {
                        continue;
                    }

                    Parameter parameter;
                    try {
                        parameter = query.getParameter(condition.getParameterName());
                    } catch (Exception e) {
                        // Não achou o parameter então é um parrameter da viewSql
                        // Nesse caso, apenas invoca parameterQueryTransform
                        parameterQueryTransform(condition);
                        continue;
                    }

                    Class<?> paramType = parameter.getParameterType();
                    Object fieldValue = condition.getValue();
                    RestField field = condition.getField();

                    if (paramType == null) {
                        if (fieldValue instanceof ArrayList<?>) {
                            paramType = ((ArrayList<?>) fieldValue).get(0).getClass();
                        } else {
                            paramType = fieldValue.getClass();
                        }
                    }

                    if (paramType == Integer.class) {
                        if (fieldValue instanceof Integer) {
                            query.setParameter(condition.getParameterName(), fieldValue);
                        } else {
                            query.setParameter(condition.getParameterName(), field.parseValueAsInteger(fieldValue, false));
                        }
                    } else if (paramType == BigDecimal.class) {
                        if (fieldValue instanceof String) {
                            query.setParameter(condition.getParameterName(), BigDecimal.valueOf(Double.parseDouble((String) fieldValue)));
                        } else {
                            query.setParameter(condition.getParameterName(), BigDecimal.valueOf((double) fieldValue));
                        }
                    } else if (paramType == Double.class || paramType == double.class) {
                        if (fieldValue instanceof Double) {
                            query.setParameter(condition.getParameterName(), fieldValue);
                        } else {
                            query.setParameter(condition.getParameterName(), field.parseValueAsDouble(fieldValue, false));
                        }
                    } else if (paramType == Long.class || paramType == long.class) {
                        if (fieldValue instanceof Long) {
                            query.setParameter(condition.getParameterName(), fieldValue);
                        } else {
                            query.setParameter(condition.getParameterName(), field.parseValueAsLong(fieldValue, false));
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
                        final String m_erro = condition.getField().getVoFieldName() + " não é uma data válida.";
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
