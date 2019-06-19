package br.unb.erlangms.rest.filter;

import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.filter.ast.RestFilterJsonAST;
import br.unb.erlangms.rest.schema.RestField;
import java.util.List;

/**
 * Implementa um visitor para localizar uma condição dado um voFieldName
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 03/04/2019
 *
 */
public class RestFilterFindByNameVisitor implements IRestFilterASTVisitor {

    private final RestField field;
    private RestFilterCondition result;

    public RestFilterFindByNameVisitor(final RestField field) {
        this.field = field;
    }

    @Override
    public Object accept(final RestFilterAST ast) {
        if (ast instanceof RestFilterJsonAST) {
            List<RestFilterCondition> conditions = ((RestFilterJsonAST) ast).getFilter();
            String voFieldName = field.getVoFieldName();
            for (RestFilterCondition condition : conditions){
                if (condition.getField().getVoFieldName().equals(voFieldName)){
                    result = condition;
                    break;
                }
            }
        }
        return null;
    }

    public RestField getField() {
        return field;
    }

    public RestFilterCondition getResult() {
        return result;
    }


}
