package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.filter.RestFilterASTCompiler;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.schema.RestFieldSortType;
import br.unb.erlangms.rest.util.RestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe responsável por prover métodos para fazer o parser dos operadores da API RESTful
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public final class RestApiRequestParser {

    private static final RestFilterASTCompiler filterASTCompiler = new RestFilterASTCompiler();

    public static List<RestField> parseFields(String fields, final IRestApiProvider apiProvider) {
        List<RestField> result = new ArrayList<>();
        if (fields != null) {
            fields = RestUtils.removeAllSpaces(fields);
            fields = RestUtils.unquoteString(fields);
            if (!fields.isEmpty()) {
                String[] fieldList = fields.split(",");
                for (String fieldName : fieldList) {
                    Optional<RestField> field = apiProvider.getContract()
                            .getSchema()
                            .getFieldByVoName(fieldName);
                    if (field.isPresent()) {
                        result.add(field.get());
                    } else {
                        throw new RestApiException(String.format(RestApiException.ATRIBUTO_IN_OPERADOR_FIELDS_NAO_EXISTE, fieldName));
                    }
                }
            }
        }
        return result;
    }

    public static List<RestField> parseSort(String sort, final IRestApiProvider apiProvider) {
        List<RestField> result = new ArrayList<>();
        if (sort != null) {
            sort = RestUtils.removeAllSpaces(sort);
            sort = RestUtils.unquoteString(sort);
            if (!sort.isEmpty()) {
                String[] sortList = sort.split(",");
                RestFieldSortType sortType = RestFieldSortType.ASC;
                for (String fieldName : sortList) {
                    if (fieldName.startsWith("-")) {
                        fieldName = fieldName.substring(1);
                        sortType = RestFieldSortType.DESC;
                    }
                    Optional<RestField> fieldOptional = apiProvider.getContract()
                            .getSchema()
                            .getFieldByVoName(fieldName);
                    if (fieldOptional.isPresent()) {
                        RestField field = fieldOptional.get();
                        field.setSortType(sortType);
                        result.add(field);
                    } else {
                        throw new RestApiException(String.format(RestApiException.ATRIBUTO_IN_OPERADOR_SORT_NAO_EXISTE, fieldName));
                    }
                }
            }
        }
        return result;
    }

    public static RestFilterAST parseFilterAST(String filter, final IRestApiProvider apiProvider) {
        return filterASTCompiler.evaluate(filter, apiProvider);
    }

}
