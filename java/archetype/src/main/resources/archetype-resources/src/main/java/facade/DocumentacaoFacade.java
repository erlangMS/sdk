#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.facade;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;
import ${package}.model.Documentacao;
import ${package}.service.SaeApplication;
 
@Singleton
@Startup
public class DocumentacaoFacade extends EmsServiceFacade {
	
	public Documentacao findById(IEmsRequest request){
		int id = request.getParamAsInt("id");
		return SaeApplication.getInstance()
			.getDocumentacaoService()
			.findById(id);
	}
	
	public List<Documentacao> find(IEmsRequest request){
		String filtro = request.getQuery("filtro");
		String fields = request.getQuery("fields");
		int limit_ini = request.getQueryAsInt("limit_ini");
		int limit_fim = request.getQueryAsInt("limit_fim");
		String sort = request.getQuery("sort");
		return SaeApplication.getInstance()
			.getDocumentacaoService()
			.find(filtro, fields, limit_ini, limit_fim, sort);
	}

	public Documentacao insert(IEmsRequest request){
		final Documentacao documentacaoPendente = (Documentacao) request.getObject(Documentacao.class);
		return SaeApplication.getInstance()
			.getDocumentacaoService()
			.insert(documentacaoPendente);
	}
	
	public Documentacao update(IEmsRequest request){
		SaeApplication saeApplication = SaeApplication.getInstance();
		int id = request.getParamAsInt("id");
		Documentacao documentacaoPendente = saeApplication.getDocumentacaoService().findById(id);
		request.mergeObjectFromPayload(documentacaoPendente);
		return saeApplication.getDocumentacaoService().update(documentacaoPendente);
	}
	
	public boolean delete(IEmsRequest request){
		final int id = request.getParamAsInt("id");
		return SaeApplication.getInstance()
			.getDocumentacaoService()
			.delete(id);
	}
	
}
