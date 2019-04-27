package br.erlangms.rest.test.provider;

import javax.persistence.Query;

import br.erlangms.rest.contract.IRestApiContract;
import br.erlangms.rest.contract.RestApiContract;
import br.erlangms.rest.filter.RestFilterCondition;
import br.erlangms.rest.provider.RestApiMetaQueryProvider;
import br.erlangms.rest.request.RestApiRequestConditionOperator;
import br.erlangms.rest.request.RestApiRequestOperator;
import br.erlangms.rest.schema.IRestApiSchema;

/**
 * Representa um provedor para um query que usa um SQL nativo e retorna dados de aluno
 *
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 01/04/2019
 */
public class AlunoGetPorNomeRestProvider extends RestApiMetaQueryProvider {
	private static final long serialVersionUID = -4335379620314743924L;
	private final String sql
                         = "SELECT E.NOME_PESSOA, F.ID_ALUNO, A.MATR_ALUNO, A.ID_CURSO_ALUNO, "
                           + "  C.COD_CURSO , B.COD_ESTRUTURADO, B.NOME_UNIDADE AS NOME_CURSO,  "
                           + "  RTRIM(C.COD_CURSO)|| RTRIM(B.NOME_UNIDADE) AS NOME_UNIDADE,  "
                           + "  RTRIM(D.NUM_VERSAO) AS NUM_VERSAO "
                           + "FROM "
                           + " DBSM.ALUNOS F LEFT JOIN DBSM.CURSOS_ALUNOS A ON (A.ID_ALUNO = F.ID_ALUNO) "
                           + " LEFT JOIN DBSM.VERSOES_CURSOS D ON (A.ID_VERSAO_CURSO = D.ID_VERSAO_CURSO) "
                           + " LEFT JOIN DBSM.CURSOS C ON (D.ID_CURSO = C.ID_CURSO) "
                           + " LEFT JOIN DBSM.ORG_INSTITUICAO B ON (B.ID_UNIDADE = C.ID_UNIDADE ), "
                           + " DBSM.PESSOAS E "
                           + "WHERE "
                           + " E.NOME_PESSOA_UP LIKE DBSM.FSEM_ACENTOS(:NOME_ALUNO) AND "
                           + " F.ID_PESSOA = E.ID_PESSOA "
                           + "UNION "
                           + "SELECT E.NOME_SOCIAL AS NOME_PESSOA, F.ID_ALUNO, A.MATR_ALUNO, A.ID_CURSO_ALUNO, "
                           + "  C.COD_CURSO , B.COD_ESTRUTURADO, B.NOME_UNIDADE AS NOME_CURSO,  "
                           + "  RTRIM(C.COD_CURSO)||RTRIM(B.NOME_UNIDADE) AS NOME_UNIDADE,  "
                           + "  RTRIM(D.NUM_VERSAO) AS NUM_VERSAO "
                           + "FROM "
                           + " DBSM.ALUNOS F LEFT JOIN DBSM.CURSOS_ALUNOS A ON (A.ID_ALUNO = F.ID_ALUNO) "
                           + " LEFT JOIN DBSM.VERSOES_CURSOS D ON (A.ID_VERSAO_CURSO = D.ID_VERSAO_CURSO) "
                           + " LEFT JOIN DBSM.CURSOS C ON (D.ID_CURSO = C.ID_CURSO) "
                           + " LEFT JOIN DBSM.ORG_INSTITUICAO B ON (B.ID_UNIDADE = C.ID_UNIDADE ), "
                           + " DBSM.PESSOAS E "
                           + "WHERE "
                           + " E.NOME_SOCIAL_UP LIKE DBSM.FSEM_ACENTOS(:NOME_ALUNO) AND "
                           + " E.NOME_SOCIAL IS NOT NULL AND"
                           + " F.ID_PESSOA = E.ID_PESSOA "
                           + "ORDER BY 1  ";

    public AlunoGetPorNomeRestProvider() {

        // Permite definir qual o viewSql que será utilizado
        setViewSql(sql);

        // Faz trim nos atributos string
        setFieldValueSerializeTransform((String fieldName, Object currentValue) -> {
            if (currentValue instanceof String) {
                return ((String) currentValue).trim();
            }
            return currentValue;
        });

        setParameterQueryCallback((RestFilterCondition condition, Query query) -> {
            // ParÃ¢metros da view não são setados pela Rest API
            if (condition.getField().getVoFieldName().equals("nome_pessoa")) {
                query.setParameter("NOME_ALUNO", condition.getValue());
            }
            return condition.getValue();
        });

        // Permite definir se o parÃ¢metro Ã© da viewSql (um parÃ¢metro que eh chumbado na prÃ³prio sql)
        setDefineIfParameterIsFromViewSql((RestFilterCondition condition) -> {
            return condition.getField().getVoFieldName().equals("nome_pessoa");
        });
    }

    @Override
    public IRestApiContract createContract() {
        IRestApiContract contract = new RestApiContract(this);

        // Define o schema do provedor
        IRestApiSchema schema = contract.getSchema();
        schema.addFieldAString("nome_pessoa", "NOME_PESSOA", 100);
        schema.addFieldAsInteger("id_aluno", "ID_ALUNO");
        schema.addFieldAString("matr_aluno", "MATR_ALUNO", 60);
        schema.addFieldAsInteger("id_curso_aluno", "ID_CURSO_ALUNO");
        schema.addFieldAString("cod_curso", "COD_CURSO", 30);
        schema.addFieldAString("codigo_estruturado", "COD_ESTRUTURADO", 30);
        schema.addFieldAString("nome_curso", "NOME_CURSO", 100);
        schema.addFieldAString("nome_unidade", "NOME_UNIDADE", 100);
        schema.addFieldAString("num_versao", "NUM_VERSAO", 30);

        //
        // Permite informar as capacidades da API para o provedor
        //

        // Por exemplo, que ela não vai aceitar operadores filter com and e or
        contract.setSupportAndOrCondition(false);

        // Vai suportar somente igualdade e contains neste operador
        contract.getSupportConditionOperators().clear();
        contract.getSupportConditionOperators().add(RestApiRequestConditionOperator.Equal);
        contract.getSupportConditionOperators().add(RestApiRequestConditionOperator.Contains);

        // Podemos definir um limite menor para o operador limit, assim o desenvolvedor está no controle
        // de quantos registros podem ser retornados
        contract.getRequestDefault().setLimit(4);

        // Como tem parÃ¢metro na viewSql, podemos obrigar o cliente a informar o filter
        contract.getRequiredApiOperators().add(RestApiRequestOperator.Filter);

        // Podemos definir uma ordenação default
        contract.getRequestDefault().setSort("nome_pessoa");

        return contract;
    }

}
