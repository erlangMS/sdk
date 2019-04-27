/*
 * 
 */
package br.erlangms.rest.query;

import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.schema.RestField;
import br.erlangms.rest.schema.RestFieldSortType;
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
    public Query createQuery(final IRestApiRequest request) {
        Query query;
        String join_fetch_smnt;
        String where_smnt;
        String field_smnt;
        String sort_smnt;
        StringBuilder sqlBuilder;
        IRestApiProvider apiProvider = request.getApiProvider();
        boolean isNativeSql = apiProvider.getViewSql() != null;

        // Verifica se o link dos dados está disponÃ­vel
        apiProvider.getApiManager().checkDataLink();

        RestJpaFilterGenerator filterGenerator = new RestJpaFilterGenerator(apiProvider);

        // Desativado porque o o hibernate atual não suporta
        //EntityGraph graph = apiProvider.getEntityGraph(entityManager);
        // Os parsers de cada parte do sql foram colocados em métodos separados para facilitar o entendimento
        where_smnt = parseWhereSmnt(request.getFilterAST(), filterGenerator);

        if (request.getDataFormat() == RestApiDataFormat.ENTITY) {
            field_smnt = "this";
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

        if (request.getDataFormat() == RestApiDataFormat.ENTITY) {
            join_fetch_smnt = parseJoinFetchSmnt(request);
            if (join_fetch_smnt != null) {
                sqlBuilder.append(join_fetch_smnt);
            }
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

        if (request.getFlags().isLogQueryCreated()) {
            LOGGER.log(Level.INFO, sql);
        }

// Desativado porque o o hibernate atual não suporta
//        if (graph != null && !graph.getAttributeNodes().isEmpty()) {
//            query.setHint("javax.persistence.loadgraph", graph);
//        }
        if (request.getOffset() != null && request.getOffset() > 0) {
            query.setFirstResult(request.getOffset());
        }

        if (request.getLimit() != null && request.getLimit() > 0) {
            query.setMaxResults(request.getLimit());
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
        String result = "this";  // O default Ã© this

        // Se informado os fields da query, então vai retornar a lista de fields para a parte select
        if (fields != null && !fields.isEmpty()) {
            StringBuilder field_smnt = new StringBuilder();
            boolean useVirgula = false;
            for (RestField field : fields) {
                if (useVirgula) {
                    field_smnt.append(",");
                }
                field_smnt.append("this.").append(field.getFieldName());
                useVirgula = true;
            }
            result = field_smnt.toString();
        }
        return result;
    }

    /**
     * Faz o parser do sort e gera o sort statement
     *
     * @param sort          lista de campos para ordenação ou null para não ordenar. Ex: "nome, cpf, rg"
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

    private String parseJoinFetchSmnt(final IRestApiRequest request) {
        String result = null;
        RestFilterAST filterAST = request.getFilterAST();
        if (filterAST != null) {
            RestJpaJoinFetchGenerator generator = new RestJpaJoinFetchGenerator(request.getApiProvider());
            filterAST.visit(generator);
            result = generator.getJoinFetchSmnt();
            if (result != null && result.isEmpty()) {
                return null;
            }
        }
        return result;
    }

}
