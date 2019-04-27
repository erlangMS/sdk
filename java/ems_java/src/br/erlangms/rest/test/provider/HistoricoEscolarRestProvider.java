/*
 * Copyright (c) 2019, CPD-UFSM. All Rights Reserved.
 */
package br.erlangms.rest.test.provider;

import br.erlangms.rest.contract.IRestApiContract;
import br.erlangms.rest.contract.RestApiContract;
import br.erlangms.rest.provider.RestApiMetaQueryProvider;
import br.erlangms.rest.schema.IRestApiSchema;

/**
 *
 * @author evertonagilar
 */
public class HistoricoEscolarRestProvider extends RestApiMetaQueryProvider {
	private static final long serialVersionUID = 4728023486940555666L;
	private final String sql = "SELECT\n"
                               + "	A.ID_PESSOA,\n"
                               + "	A.NOME_PESSOA,\n"
                               + "	B.ID_ALUNO,\n"
                               + "	C.MATR_ALUNO,\n"
                               + "	D.NUM_VERSAO,\n"
                               + "   	G.NOME_UNIDADE AS NOME_CURSO,\n"
                               + "	F.COD_CURSO,\n"
                               + "	C.ID_VERSAO_CURSO,\n"
                               + "	E.ANO,\n"
                               + "	RTRIM(I.COD_ATIV_CURRIC) as COD_ATIV_CURRIC,\n"
                               + "	RTRIM(I.NOME_ATIV_CURRIC) as NOME_ATIV_CURRIC,\n"
                               + "	E.CREDITOS,\n"
                               + "	coalesce(E.MEDIA_FINAL,1000000) as MEDIA_FINAL,\n"
                               + "	J.DESCRICAO AS DESCR_SITUACAO,       H.DESCRICAO AS PERIODO,\n"
                               + "	C.ID_CURSO_ALUNO,	E.SITUACAO_ITEM,\n"
                               + "       	I.CH_TEORICA,	I.CH_PRATICA,\n"
                               + "       	I.CH_TOTAL AS TOTAL_CARGA_HORARIA,\n"
                               + "	T1.DESCRICAO AS FORMA_INGRESSO,\n"
                               + "	C.ANO_INGRESSO,\n"
                               + "	T2.DESCRICAO AS FORMA_EVASÃO,\n"
                               + "	C.ANO_EVASAO AS ANO_EVASÃO,\n"
                               + "        B.SEXO,  RTRIM(EC.DESCR_ESTRUTURA) AS DESCR_ESTRUTURA, F.ID_CURSO, C.FORMA_EVASAO_ITEM "
                               + "FROM   DBSM.PESSOAS A, \n"
                               + "       DBSM.ALUNOS B ,\n"
                               + "       DBSM.VERSOES_CURSOS D,\n"
                               + "       DBSM.CURSOS F,\n"
                               + "       DBSM.ORG_INSTITUICAO G,\n"
                               + "       DBSM.CURSOS_ALUNOS C,\n"
                               + "       DBSM.CURRICULO_ALUNO E,\n"
                               + "       DBSM.TAB_ESTRUTURADA H,\n"
                               + "       DBSM.ATIVIDADES_CURRIC I,\n"
                               + "       DBSM.TAB_ESTRUTURADA J,\n"
                               + "	DBSM.TAB_ESTRUTURADA T1,\n"
                               + "	DBSM.TAB_ESTRUTURADA T2,\n"
                               + "	DBSM.ESTRUTURA_CURRIC EC, DBSM.ATIVIDADES_CURRIC AC\n"
                               + " WHERE \n"
                               //                               + "	F.ID_CURSO = :ID_CURSO AND\n"
                               + "       	E.SITUACAO_OCOR<>'E' AND\n"
                               //                               + "	C.FORMA_EVASAO_ITEM = :FORMA_EVASAO_ITEM AND\n"
                               + "	C.ID_ALUNO = B.ID_ALUNO AND\n"
                               + "       	B.ID_PESSOA = A.ID_PESSOA AND       	\n"
                               + "       	C.ID_VERSAO_CURSO = D.ID_VERSAO_CURSO AND \n"
                               + "	D.ID_CURSO = F.ID_CURSO AND\n"
                               + "       	F.ID_UNIDADE = G.ID_UNIDADE AND\n"
                               + "       	E.PERIODO_TAB = H.COD_TABELA AND \n"
                               + "	E.PERIODO_ITEM = H.ITEM_TABELA AND\n"
                               + "       	I.ID_ATIV_CURRIC = E.ID_ATIV_CURRIC AND\n"
                               + "       	J.COD_TABELA = E.SITUACAO_TAB AND \n"
                               + "	J.ITEM_TABELA = E.SITUACAO_ITEM AND\n"
                               + "       	E.ID_CURSO_ALUNO=C.ID_CURSO_ALUNO AND\n"
                               + "	C.FORMA_INGRE_TAB = T1.COD_TABELA AND\n"
                               + "	C.FORMA_INGRE_ITEM = T1.ITEM_TABELA AND\n"
                               + "	C.FORMA_EVASAO_TAB = T2.COD_TABELA AND\n"
                               + "	C.FORMA_EVASAO_ITEM = T2.ITEM_TABELA AND\n"
                               //                               + "        C.ANO_INGRESSO >= :ANO_INGRESSO AND\n"
                               + "        E.ID_ATIV_CURRIC=AC.ID_ATIV_CURRIC AND\n"
                               + "        E.ID_ESTRUTURA_CUR=EC.ID_ESTRUTURA_CUR\n"
                               + "       \n"
                               + "	\n"
                               + "ORDER BY 6,4,9,15,11";

    public HistoricoEscolarRestProvider() {
        setViewSql(sql);

        // Faz trim nos atributos string
        setFieldValueSerializeTransform((String fieldName, Object currentValue) -> {
            if (currentValue instanceof String) {
                return ((String) currentValue).trim();
            }
            return currentValue;
        });

        // Permite fazer uma transformaÃ§Ã£o no parÃ¢metro que vai ser setado na query durante a execução
/* setParameterQueryCallback((RestFilterCondition condition, Query query) -> {
         * // ParÃ¢metros da view não são setados pela Rest API
         * switch (condition.getField().getVoFieldName()) {
         * case "id_curso":
         * query.setParameter("ID_CURSO", condition.getValue());
         * break;
         * case "forma_evasao_item":
         * query.setParameter("FORMA_EVASAO_ITEM", condition.getValue());
         * break;
         * case "ano_ingresso":
         * query.setParameter("ANO_INGRESSO", condition.getValue());
         * break;
         * }
         *
         * return condition.getValue();
         * });
         *
         * // Permite definir se o parÃ¢metro Ã© da viewSql (um parÃ¢metro que eh chumbado na prÃ³prio sql)
         * setDefineIfParameterIsFromViewSql((RestFilterCondition condition) -> {
         * String voFieldName = condition.getField().getVoFieldName();
         * return voFieldName.equals("id_curso")
         * || voFieldName.equals("forma_evasao_item")
         * || voFieldName.equals("ano_ingresso");
         * }); */
    }

    @Override
    public IRestApiContract createContract() {
        IRestApiContract contract = new RestApiContract(this);

        // Define o schema do provedor
        IRestApiSchema schema = contract.getSchema();
        schema.addFieldAsInteger("id_curso", "ID_CURSO");
        schema.addFieldAsInteger("id_aluno", "ID_ALUNO", true);
        schema.addFieldAsInteger("id_pessoa", "ID_PESSOA");
        schema.addFieldAString("nome_pessoa", "NOME_PESSOA", 100);
        schema.addFieldAString("matr_aluno", "MATR_ALUNO", 30);
        schema.addFieldAsInteger("cod_curso", "COD_CURSO");
        schema.addFieldAsInteger("ano", "ANO");
        schema.addFieldAsInteger("cod_ativ_curric", "COD_ATIV_CURRIC");
        schema.addFieldAString("descr_situacao", "DESCR_SITUACAO", 100);
        schema.addFieldAsInteger("media_final", "MEDIA_FINAL");
        schema.addFieldAString("periodo", "PERIODO", 60);
        schema.addFieldAsInteger("ano_ingresso", "ANO_INGRESSO");
        schema.addFieldAString("num_versao", "NUM_VERSAO", 30);
        schema.addFieldAsInteger("forma_evasao_item", "FORMA_EVASAO_ITEM");
        schema.addFieldAString("nome_curso", "NOME_CURSO", 100);

        return contract;
    }

}
