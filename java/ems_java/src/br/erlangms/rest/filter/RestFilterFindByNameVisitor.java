/*
 * 
 */
package br.erlangms.rest.filter;

import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.filter.ast.RestFilterJsonAST;
import java.util.Map;

/**
 * Implementa um visitor para localizar uma filtro específico
 * dado o root da AST do operador filter
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 03/04/2019
 *
 */
public class RestFilterFindByNameVisitor implements IRestFilterASTVisitor {
	private static final long serialVersionUID = 461168163915755088L;
	private final String fieldName;
    private final Object defaultValue;

    public RestFilterFindByNameVisitor(String fieldName, Object defaultValue) {
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object accept(final RestFilterAST ast) {
        if (ast instanceof RestFilterJsonAST) {
            RestFilterJsonAST filterJsonAST = (RestFilterJsonAST) ast;
            Map<String, Object> map = filterJsonAST.getFilterMap();
            if (map.containsKey(fieldName)){
                return map.getOrDefault(fieldName, defaultValue);
            }
        }
        return null;
    }

}
