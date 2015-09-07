package br.erlangms.fpc.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;

@Singleton
@Startup
public class FpcService extends EmsServiceFacade {
	
	public FpcService() throws Exception {
		super();
	}

	public String get(IEmsRequest request){
		final String db_table = request.getQuery("db_table");
		final int pk = Integer.parseInt(request.getQuery("pk"));
		String jstr = "";
		switch (pk) {
			case 1: 
				jstr = "{\"id\": \"1\", \"aluno_id\": \"2341\", \"dataFim\": \"12/12/2017\", \"dataInicio\": \"01/07/2016\", \"semestreAno\": \"2016\"}";
				break;
			case 2: jstr =  "{\"id\": \"2\", \"aluno_id\": \"12345\", \"dataFim\": \"13/11/2016\", \"dataInicio\": \"12/06/2011\", \"semestreAno\": \"2017\"}";
				break;
			case 3: jstr =  "{\"id\": \"3\", \"aluno_id\": \"5336\", \"dataFim\": \"30/04/2016\", \"dataInicio\": \"03/04/2014\", \"semestreAno\": \"2014\", \"suspendeBA\":\"1\"}";
		}
		return jstr;
	}
	
	public String pesquisar(IEmsRequest request){
		final String db_table = request.getQuery("db_table");
		final String filtro = request.getQuery("filtro");
		final String fields = request.getQuery("fields");
		final String limit_ini = request.getQuery("limit_ini");
		final String limit_fim = request.getQuery("limit_fim");

		return "{\"draw\": 1,\"recordsTotal\": \"1\",\"recordsFiltered\": \"1\",\"data\" : " +
				   "["+
				 	   "[\"<input type='radio' name='f_id' value='1'/>\",\"12341\",\"2011\",\"2013-11-01\",\"2014-12-30\",\"1\"],"+
				 	   "[\"<input type='radio' name='f_id' value='2'/>\",\"19324\",\"2015\",\"2014-07-01\",\"2014-12-30\",\"0\"],"+
				 	   "[\"<input type='radio' name='f_id' value='3'/>\",\"4245\",\"2018\",\"2016-05-01\",\"2016-12-30\",\"1\"]"+
				   "]"+
				 "}";
		
	}
	
	public String existeCampoDuplicado(IEmsRequest request){
		final String db_table = request.getQuery("db_table");
		final String pk = request.getQuery("pk");
		final String field_name = request.getQuery("field_name");
		final String field_value = request.getQuery("field_value");
		return "false";
	}
	
	private static int i = 0;
	public String sequence(IEmsRequest request){
		final String db_table = request.getQuery("db_table");
		final String field_name = request.getQuery("field_name");
		return Integer.toString(i++);
	}

	public String save(IEmsRequest request){
		final String db_table = request.getQuery("db_table");
		final String pk = request.getQuery("pk");
		final String update_fields = request.getPayload();
		return "ok";
	}
	
}
