package br.erlangms.rest.filter;

import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.exception.RestParserException;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.erlangms.rest.filter.ast.RestFilterOpPrecedenciaAST;
import br.erlangms.rest.filter.tokens.RestFilterJsonToken;
import br.erlangms.rest.filter.tokens.RestFilterOpToken;
import br.erlangms.rest.filter.tokens.RestFilterToken;
import br.erlangms.rest.provider.IRestApiProvider;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * EXPRESSION = TERM [ OP_LOGICO EXPRESSION ]
 * OP_LOGICO = EXPRESSION "or"/"and" TERM
 * TERM = JSON_EXPRESSION | PARENTHESIZED_EXPRESSION
 * JSON_EXPRESSION = "json string"
 * PARENTHESIZED_EXPRESSION = "(" EXPRESSION ")"
 *
 * Exemplo 1: { "nome__contains" : "AGILAR" } and ({ "tipo_sanguineo" : "AB" } or { "tipo_sanguineo" : "B" })
 * Exemplo 2: { "nome" : "EVERTON DE VARGAS AGILAR" }
 * Exemplo 3: { "tipo_sanguineo" : "AB", "fator_rh__isnull" : true}
 *
 */
public class RestFilterParser implements IRestFilterParser {

    private final IRestApiProvider apiProvider;

    public RestFilterParser(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    @Override
    public RestFilterAST parse(final List<RestFilterToken> tokens) {
        ListIterator<RestFilterToken> tokenIter = tokens.listIterator();

        // **********
        // Se não há tokens ou há apenas um token de uma expressão vazia
        // então não será gerado a AST pois é como se nenhum filtro fosse passado
        // **********
        if (!tokenIter.hasNext() ||
            (tokens.size() == 1 && tokens.get(0).getValue().equals("{}"))) {
                return null;
        }

        RestFilterAST expr = parseExpression(tokenIter);
        if (tokenIter.hasNext()) {
            throw new RestParserException("Texto extra no filter: " + tokenIter.next().getValue());
        }
        return expr;
    }

    private RestFilterAST parseExpression(final ListIterator<RestFilterToken> tokenIter) {
        RestFilterAST expr = parseTerm(tokenIter);
        while (tokenIter.hasNext()) {
            RestFilterToken op = tokenIter.next();
            if (op instanceof RestFilterOpToken) {
                expr = parseOpLogicoExpression(expr, (RestFilterOpToken) op, tokenIter);
            } else {
                tokenIter.previous();
                break;
            }
        }
        return expr;
    }

    private RestFilterAST parseTerm(final ListIterator<RestFilterToken> tokenIter) {
        if (!tokenIter.hasNext()) {
            throw new RestParserException("Fim prematuro do filter.");
        }
        RestFilterToken t = tokenIter.next();
        if (t instanceof RestFilterJsonToken) {
            return parseJsonExpression((RestFilterJsonToken) t);
        } else if (t.getValue().equals("(")) {
            return parseParenthesizedExpression(tokenIter);
        } else {
            throw new RestParserException("Esperado uma expressão json, recebido " + t.getValue() + ".");
        }
    }

    private RestFilterAST parseOpLogicoExpression(final RestFilterAST leftExpr, final RestFilterOpToken op, final ListIterator<RestFilterToken> tokenIter) {
        if (!apiProvider.getContract().isSupportAndOrCondition()){
            throw new RestApiConstraintException("Operador and/or não permitido para o webservice solicitado.");
        }
        return op.toExpression(leftExpr, parseTerm(tokenIter));
    }

    private RestFilterAST parseJsonExpression(final RestFilterJsonToken t) {
        return new RestFilterJsonAST(t, apiProvider);
    }

    private RestFilterAST parseParenthesizedExpression(final ListIterator<RestFilterToken> tokenIter) {
        RestFilterAST innerExpr = parseExpression(tokenIter);
        RestFilterAST expr = new RestFilterOpPrecedenciaAST(innerExpr);
        if (!tokenIter.hasNext() || !")".equals(tokenIter.next().getValue())) {
            throw new RestParserException("Parênteses de fechamento omitido.");
        }
        return expr;
    }
}
