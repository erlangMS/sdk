package br.erlangms.samples.service;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;
import br.erlangms.samples.negocio.PessoaNegocio;
import br.erlangms.samples.pojo.Pessoa;

@Singleton
@Startup
public class PessoaService extends EmsServiceFacade {
	
	public PessoaService() throws Exception {
		super();
	}

	public List<Pessoa> lista(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		List<Pessoa> lista = negocio.getLista();
		return lista;
	}
	
	public Pessoa findById(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		return negocio.findById(request.getParamAsInt("id"));
	}
	
	public void atualiza(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		int id = request.getParamAsInt("id");
		Pessoa pessoa = (Pessoa) request.getObject(Pessoa.class);
		pessoa.setIdade(id);
		negocio.update(pessoa);
	}
	
	public void insert(IEmsRequest request){
		PessoaNegocio negocio = new PessoaNegocio();
		Pessoa pessoa = (Pessoa) request.getObject(Pessoa.class);
		negocio.insert(pessoa);
	}	
}
