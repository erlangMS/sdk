/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.filter.IRestFilterASTGenerator;
import br.erlangms.rest.filter.RestFilterCondition;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.ast.RestFilterAndAST;
import br.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.erlangms.rest.filter.ast.RestFilterOrAST;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.util.RestUtils;
import java.util.List;

/**
 * Implementa o gerador de código para a condição where do operador filter
 *
 * @author Everton de Vargas Agilar
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
            switch (condition.getOperator()) {
                case IsNull:
                    if (!isParameterFromViewSql(condition)) {
                        boolean boolValue = RestUtils.parseAsBoolean(condition.getValue());
                        if (boolValue) {
                            code.append(condition.getField().getFieldName()).append(" is null ");
                        } else {
                            code.append(condition.getField().getFieldName()).append(" is not null ");
                        }
                        useAnd = true;
                        codeTransform = emitCodeTransform(condition, code.toString());
                        where.append(codeTransform);
                    }
                    break;
                case IContains:
                case ILike:
                case INotContains:
                case INotLike:
                    if (!isParameterFromViewSql(condition)) {
                        condition.setSqlFieldName(String.format("lower(this.%s)", condition.getField().getFieldName()));
                        ++parameterCount;
                        code.append(condition.getSqlFieldName())
                                .append(condition.getSqlOperator())
                                .append(":").append(condition.getParameterName());
                        codeTransform = emitCodeTransform(condition, code.toString());
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
                        codeTransform = emitCodeTransform(condition, code.toString());
                        useAnd = true;
                        where.append(codeTransform);
                    }
                    break;
                default:
                    if (!isParameterFromViewSql(condition)) {
                        condition.setSqlFieldName(String.format("this.%s", condition.getField().getFieldName()));
                        ++parameterCount;
                        code.append(condition.getSqlFieldName())
                                .append(condition.getSqlOperator())
                                .append(":").append(condition.getParameterName());
                        codeTransform = emitCodeTransform(condition, code.toString());
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

    private String emitCodeTransform(final RestFilterCondition condition, final String codeGenerated) {
        RestJpaFilterGeneratorEmitCodeTransform f = apiProvider.getEmitCodeTransform();
        if (f != null) {
            return f.emitCode(condition, codeGenerated);
        }
        return codeGenerated;
    }

}
