package br.erlangms.rest.test.provider;

import br.erlangms.rest.contract.IRestApiContract;
import br.erlangms.rest.contract.RestApiContract;
import br.erlangms.rest.provider.RestApiEntityProvider;
import br.erlangms.rest.request.IRestApiRequest;
import br.erlangms.rest.schema.IRestApiSchema;

/**
 *
 * @author evertonagilar
 */
public class AlunoRestProvider extends RestApiEntityProvider {
	private static final long serialVersionUID = -4686436392270530071L;

	public AlunoRestProvider() {
        super(Aluno.class);
    }

    @Override
    public IRestApiContract createContract() {
        IRestApiContract contract = new RestApiContract(this);

        // Define se suporta namedQuery (default true)
        contract.setSupportNamedQuery(false);
        contract.getCachePolicyConfig().setAllowResultCache(false);

        // Define requisiÃ¯Â¿Â½Ã¯Â¿Â½o default do provedor
        IRestApiRequest requestDefault = contract.getRequestDefault();
        requestDefault.setLimit(15);
        requestDefault.setMaxLimit(150);
        requestDefault.setSort("pk");

        // Define o esquema do provedor
        IRestApiSchema schema = contract.getSchema();
        schema.addFieldAsInteger("id", "id");
        schema.addFieldAsInteger("id_pessoa", "pessoa.id");
        schema.addFieldAString("nome", "pessoa.nome", 100);
        schema.addFieldAString("nome_pessoa", "pessoa.nome", 100);
        schema.addFieldAString("sexo", "sexo", 1);
        schema.addFieldAString("nome_pai", "nomePai", 100);
        schema.addFieldAString("nome_mae", "nomeMae", 100);
        schema.addFieldAString("pai", "nomePai", 100);
        schema.addFieldAString("fator_rh", "fatorRH", 30);
        schema.addFieldAString("tipo_sanguineo", "tipoSanguineo", 30);
        schema.addFieldAString("url", "url", 200);

        return contract;
    }



}
