package br.unb.erlangms.rest.query;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.filter.IRestFilterASTGenerator;
import br.unb.erlangms.rest.filter.RestFilterCondition;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.filter.ast.RestFilterAndAST;
import br.unb.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.unb.erlangms.rest.filter.ast.RestFilterOrAST;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.RestApiRequestConditionOperator;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.schema.RestFieldCharCase;
import br.unb.erlangms.rest.util.RestUtils;
import java.util.List;

/**
 * Implementa o gerador de código para a condição where do operador filter
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 20/02/2019
 *
 */
public class RestJpaFilterGenerator implements IRestFilterASTGenerator {

    private final IRestApiProvider apiProvider;
    private int parameterCount;

    public RestJpaFilterGenerator(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        this.parameterCount = 0;
    }

    @Override
    public String emitCode(final RestFilterAST ast) {
        if (ast instanceof RestFilterJsonAST) {
            return emitCodeFilter(((RestFilterJsonAST) ast).getFilter());
        } else if (ast instanceof RestFilterAndAST) {
            return " and ";
        } else if (ast instanceof RestFilterOrAST) {
            return " or ";
        }
        return null;
    }

    private String emitCodeFilter(final List<RestFilterCondition> filterConditions) {
        boolean useAnd = false;
        StringBuilder where = new StringBuilder();
        for (RestFilterCondition condition : filterConditions) {
            if (useAnd) {
                where.append(" and ");
            }
            StringBuilder code = new StringBuilder();
            String codeTransform;
            condition.setSqlOperator(RestApiRequestConditionOperator.fieldOperatorToSqlOperator(condition.getOperator()));
            RestField field = condition.getField();

            // Atributos com expressão não devem aparecer no operador filter
            if (field.getValueExpression() != null){
                throw new RestApiException(RestApiException.ATRIBUTO_EXPRESSION_NAO_DEVE_APARECER_OPERADOR_FILTER, field.getVoFieldName());
            }

            switch (condition.getOperator()) {
                case IsNull:
                    if (!isParameterFromViewSql(condition)) {
                        boolean boolValue = RestUtils.parseAsBoolean(condition.getValue());
                        if (boolValue) {
                            code.append(field.getFieldName()).append(" is null ");
                        } else {
                            code.append(field.getFieldName()).append(" is not null ");
                        }
                        useAnd = true;
                        codeTransform = emitCodeCallback(condition, code.toString());
                        where.append(codeTransform);
                    }
                    break;
                case IContains:
                case ILike:
                case INotContains:
                case INotLike:
                    if (!isParameterFromViewSql(condition)) {
                        condition.setSqlFieldName(String.format("lower(this.%s)", field.getFieldName()));
                        ++parameterCount;
                        code.append(condition.getSqlFieldName())
                                .append(condition.getSqlOperator())
                                .append(":").append(condition.getParameterName());
                        codeTransform = emitCodeCallback(condition, code.toString());
                        useAnd = true;
                        where.append(codeTransform);
                    }
                    break;
                case In:
                    if (!isParameterFromViewSql(condition)) {
                        condition.setSqlFieldName(String.format("this.%s", condition.getField().getFieldName()));
                        ++parameterCount;
                        code.append(condition.getSqlFieldName())
                                .append(condition.getSqlOperator())
                                .append(":").append(condition.getParameterName());
                        codeTransform = emitCodeCallback(condition, code.toString());
                        useAnd = true;
                        where.append(codeTransform);
                    }
                    break;
                default:
                    if (!isParameterFromViewSql(condition)) {
                        if (field.getCharCase() == null || field.getCharCase() == RestFieldCharCase.NORMAL) {
                            condition.setSqlFieldName(String.format("this.%s", field.getFieldName()));
                        } else {
                            switch (condition.getField().getCharCase()) {
                                case UPPERCASE:
                                    condition.setSqlFieldName(String.format("upper(this.%s)", field.getFieldName()));
                                    break;
                                case LOWERCASE:
                                    condition.setSqlFieldName(String.format("lower(this.%s)", field.getFieldName()));
                                    break;
                            }
                        }
                        ++parameterCount;
                        code.append(condition.getSqlFieldName())
                                .append(condition.getSqlOperator())
                                .append(":").append(condition.getParameterName());
                        codeTransform = emitCodeCallback(condition, code.toString());
                        useAnd = true;
                        where.append(codeTransform);
                    }
                    break;
            }
        }
        return where.toString();
    }

    public int getParameterCount() {
        return parameterCount;
    }

    private boolean isParameterFromViewSql(RestFilterCondition condition) {
        RestJpaFilterGeneratorDefineIfParameterIsFromViewSql f = apiProvider.getDefineIfParameterIsFromViewSql();
        if (f != null) {
            return f.defineIfParameterIsFromViewSql(condition);
        }
        return false;
    }

    private String emitCodeCallback(final RestFilterCondition condition, final String codeGenerated) {
        RestJpaFilterGeneratorEmitCodeCallback f = apiProvider.getEmitCodeFilterConditionCallback();
        if (f != null) {
            return f.execute(condition, codeGenerated);
        }
        return codeGenerated;
    }

}
