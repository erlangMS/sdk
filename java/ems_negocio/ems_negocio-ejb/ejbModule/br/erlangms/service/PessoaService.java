package br.erlangms.service;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import br.erlangms.IEmsRequest;
import br.erlangms.negocio.PessoaNegocio;
import br.erlangms.pojo.Pessoa;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@LocalBean
@Startup
public class PessoaService extends EmsServiceFacade {
	
	
	public PessoaService() throws Exception {
		super();
	}

	static public String getLista(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		List<Pessoa> lista = negocio.getLista();
		String result = toJson(lista);
		System.out.println(result);
		return result;
	}
	
	static public String findById(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		Pessoa p = negocio.findById(request.getParamAsInt("id"));
		String result = toJson(p);
		System.out.println(result);
		return result;
	}
	
}
