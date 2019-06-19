package br.unb.erlangms.rest.query;

import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import br.unb.erlangms.rest.schema.RestField;
import br.unb.erlangms.rest.schema.RestFieldSortType;
import br.unb.erlangms.rest.schema.RestFieldType;
import br.unb.erlangms.rest.schema.RestJoinType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Classe responsável por gerar a query JPQL da requisição REST
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 12/03/2019
 *
 */
public final class RestJpaQueryGenerator implements IRestQueryGenerator {
    private static final Logger LOGGER = Logger.getLogger(RestJpaQueryGenerator.class.getName());

    @Override
    public Query createQuery(final IRestApiRequestInternal request) {
        Query query;
        String join_fetch_smnt;
        String where_smnt;
        String field_smnt;
        String sort_smnt;
        StringBuilder sqlBuilder;
        IRestApiProvider apiProvider = request.getApiProvider();
        boolean isNativeSql = apiProvider.getViewSql() != null;

        RestJpaFilterGenerator filterGenerator = new RestJpaFilterGenerator(apiProvider);

        // Desativado porque o o hibernate atual não suporta
        //EntityGraph graph = apiProvider.getEntityGraph(entityManager);
        where_smnt = parseWhereSmnt(request.getFilterAST(), filterGenerator);

        if (request.getApiDataFormat() == RestApiDataFormat.ENTITY || request.getFlags().isAllFields()) {
            if (isNativeSql) {
                field_smnt = "*";
            } else {
                field_smnt = "this";
            }
        } else {
            field_smnt = parseFieldsSmnt(request.getFieldsList());
        }

        sort_smnt = parseSortSmnt(request.getSortList());

        if (isNativeSql) {
            sqlBuilder = new StringBuilder("select ")
                    .append(field_smnt == null ? "*" : field_smnt)
                    .append(" from (").append(apiProvider.getViewSql()).append(")")
                    .append(" this ");

        } else {
            sqlBuilder = new StringBuilder("select ")
                    .append(field_smnt)
                    .append(" from ").append(apiProvider.getEntityClass().getSimpleName()).append(" this ");
        }

        //if (request.getApiDataFormat() == RestApiDataFormat.ENTITY) {
        if (request.getRequestUser().getFlags().getJoinType() == null
            || request.getRequestUser().getFlags().getJoinType() != RestJoinType.NO_JOIN) {
            join_fetch_smnt = parseJoinFetchSmnt(request);
            if (join_fetch_smnt != null) {
                sqlBuilder.append(join_fetch_smnt);
            }
            // }
        }

        if (where_smnt != null) {
            sqlBuilder.append(" where ");
            sqlBuilder.append(where_smnt);
        }

        if (sort_smnt != null) {
            sqlBuilder.append(sort_smnt);
        }

        String sql = sqlBuilder.toString();

        EntityManager entityManager = apiProvider.getApiManager().getEntityManager();

        if (isNativeSql) {
            query = entityManager.createNativeQuery(sql);
        } else {
            query = entityManager.createQuery(sql);
        }

        if (request.getFlags().isLogQuery()) {
            LOGGER.log(Level.INFO, sql);
        }

// Desativado porque o o hibernate atual não suporta
//        if (graph != null && !graph.getAttributeNodes().isEmpty()) {
//            query.setHint("javax.persistence.loadgraph", graph);
//        }
        if (!request.getFlags().isNoPaginate()) {
            if (request.getOffset() != null && request.getOffset() > 0) {
                query.setFirstResult(request.getOffset());
            }

            if (request.getLimit() != null && request.getLimit() > 0) {
                query.setMaxResults(request.getLimit());
            }
        }

        return query;
    }

    /**
     * Faz o parser dos fields e gera o bloco de campos do select
     *
     * @param fields        lista de campos que devem retornar ou o objeto inteiro se null. Ex: "nome, cpf, rg"
     * @param apiProvider   mapeador de entidades
     * @param entityManager
     * @return String
     * @author Everton de Vargas Agilar
     */
    private String parseFieldsSmnt(List<RestField> fields) {
        String result = "this";  // O default é this

        // Se informado os fields da query, então vai retornar a lista de fields para a parte select
        if (fields != null && !fields.isEmpty()) {
            StringBuilder field_smnt = new StringBuilder();
            boolean useVirgula = false;
            for (RestField field : fields) {
                if (useVirgula) {
                    field_smnt.append(",");
                }
                if (field.getValueExpression() != null) {
                    if (field.getFieldType() == RestFieldType.STRING
                        || field.getFieldType() == RestFieldType.DATE) {
                        field_smnt.append("'").append(field.getValueExpression()).append("' as ").append(field.getVoFieldName());
                    } else {
                        field_smnt.append(field.getValueExpression()).append(" as ").append(field.getVoFieldName());
                    }
                } else {
                    field_smnt.append("this.").append(field.getFieldName()).append(" as ").append(field.getVoFieldName());
                }
                useVirgula = true;
            }
            result = field_smnt.toString();
        }
        return result;
    }

    /**
     * Faz o parser do sort e gera o sort statement
     *
     * @param sort          lista de campos para odenação ou null para não ordenar. Ex: "nome, cpf, rg"
     * @param apiProvider   mapeador de entidades
     * @param entityManager
     * @return String sort statement
     * @author Everton de Vargas Agilar
     */
    private String parseSortSmnt(final List<RestField> sort) {
        String result = null;      // por default não tem ordenação

        // Se informado os campos para sort, então vai gerar o sort statement do select
        if (sort != null && !sort.isEmpty()) {
            boolean useVirgula = false;
            StringBuilder sort_smnt = new StringBuilder(" order by");
            for (RestField field : sort) {

                // Atributos com expressão não devem aparecer no operador sort
                if (field.getValueExpression() != null) {
                    throw new RestApiException(RestApiException.ATRIBUTO_EXPRESSION_NAO_DEVE_APARECER_OPERADOR_SORT, field.getVoFieldName());
                }

                if (useVirgula) {
                    sort_smnt.append(",");
                }
                if (field.getSortType() == RestFieldSortType.DESC) {
                    sort_smnt.append(" this.").append(field.getFieldName()).append(" desc");
                } else {
                    sort_smnt.append(" this.").append(field.getFieldName());
                }
                useVirgula = true;
            }
            result = sort_smnt.toString();
        }
        return result;
    }

    private String parseWhereSmnt(final RestFilterAST filterAST,
                                  final RestJpaFilterGenerator filterGenerator) {
        String result = null;
        if (filterAST != null) {
            result = filterAST.evaluate(filterGenerator);
            if (result != null && result.isEmpty()) {
                return null;
            }
        }
        return result;
    }

//    private String parseJoinFetchSmnt(final IRestApiRequestInternal request) {
//        String result = null;
//        RestFilterAST filterAST = request.getFilterAST();
//        if (filterAST != null) {
//            RestJpaJoinFetchGenerator generator = new RestJpaJoinFetchGenerator(request);
//            filterAST.visit(generator);
//            result = generator.getJoinFetchSmnt();
//            if (result != null && result.isEmpty()) {
//                return null;
//            }
//        }
//        return result;
//    }
    private String parseJoinFetchSmnt(final IRestApiRequestInternal request) {
        RestJoinType joinTypeFlag = request.getRequestUser().getFlags().getJoinType();
        StringBuilder joinFetch = new StringBuilder();
        List<String> attrBaseRelationNameFetch = new ArrayList<>();
        for (RestField field : request.getFieldsList()) {
            String attrBaseRelationName = field.getAttrBaseRelationName();
            if (field.isAttributeObject() && !attrBaseRelationNameFetch.contains(attrBaseRelationName)) {
                if (joinTypeFlag != null) {
                    if (joinTypeFlag == RestJoinType.LEFT_JOIN) {
                        joinFetch.append(" left join this.");
                    } else if (joinTypeFlag == RestJoinType.RIGHT_JOIN) {
                        joinFetch.append(" right join this.");
                    } else if (joinTypeFlag == RestJoinType.NO_JOIN) {
                        continue;
                    } else {
                        joinFetch.append(" join this.");
                    }
                } else {
                    if (field.getJoinType() == RestJoinType.LEFT_JOIN) {
                        joinFetch.append(" left join this.");
                    } else if (field.getJoinType() == RestJoinType.RIGHT_JOIN) {
                        joinFetch.append(" right join this.");
                    } else if (joinTypeFlag == RestJoinType.NO_JOIN) {
                        continue;
                    } else {
                        joinFetch.append(" join this.");
                    }
                }
                joinFetch.append(attrBaseRelationName);
                attrBaseRelationNameFetch.add(attrBaseRelationName);
            }
        }
        return joinFetch.toString();
    }
}
