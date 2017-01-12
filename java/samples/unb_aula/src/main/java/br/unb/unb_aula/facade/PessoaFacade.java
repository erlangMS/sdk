package br.unb.unb_aula.facade;

import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;
import br.unb.unb_aula.model.Pessoa;
import br.unb.unb_aula.service.ModApplication;
 
@Singleton
@Startup
public class PessoaFacade extends EmsServiceFacade {
	
	public Pessoa findById(IEmsRequest request){
		int id = request.getParamAsInt("id");
		return ModApplication.getInstance()
			.getPessoaService()
			.findById(id);
	}
	
	public List<Pessoa> find(IEmsRequest request){
		String filter = request.getQuery("filter");
		String fields = request.getQuery("fields");
		int limit = request.getQueryAsInt("limit");
		int offset = request.getQueryAsInt("offset");
		String sort = request.getQuery("sort");
		return ModApplication.getInstance()
			.getPessoaService()
			.find(filter, fields, limit, offset, sort);
	}

	public Pessoa insert(IEmsRequest request){
		final Pessoa Pessoa = (Pessoa) request.getObject(Pessoa.class);
		return ModApplication.getInstance()
			.getPessoaService()
			.insert(Pessoa);
	}
	
	public Pessoa update(IEmsRequest request){
		int id = request.getParamAsInt("id");
		ModApplication app = ModApplication.getInstance();
		Pessoa Pessoa = app.getPessoaService().findById(id);
		request.mergeObjectFromPayload(Pessoa);
		return app.getPessoaService().update(Pessoa);
	}
	
	public boolean delete(IEmsRequest request){
		final int id = request.getParamAsInt("id");
		return ModApplication.getInstance()
			.getPessoaService()
			.delete(id);
	}

	public Double getCredito(IEmsRequest request){
		int id = request.getParamAsInt("id");
		return ModApplication.getInstance()
			.getPessoaService()
			.getCredito(id);
	}

	public Double inserirCredito(IEmsRequest request){
		int id = request.getParamAsInt("id");
		Map<String, Object> payload = request.getPayloadAsMap();
		Double valor = (Double) payload.get("valor");
		return ModApplication.getInstance()
			.getPessoaService()
			.inserirCredito(id, valor);
	}
	
}
