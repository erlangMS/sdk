package br.erlangms.service;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.IEmsRequest;
import br.erlangms.negocio.PessoaNegocio;
import br.erlangms.pojo.Pessoa;

@Singleton
@Startup
public class PessoaService extends EmsServiceFacade {
	
	public PessoaService() throws Exception {
		super();
	}

	static public List<Pessoa> getLista(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		List<Pessoa> lista = negocio.getLista();
		return lista;
	}
	
	static public Pessoa findById(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		return negocio.findById(request.getParamAsInt("id"));
	}
	
}
