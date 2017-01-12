package br.unb.unb_aula.facade;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;
import br.unb.unb_aula.model.Curso;
import br.unb.unb_aula.service.ModApplication;
 
@Singleton
@Startup
public class CursoFacade extends EmsServiceFacade {
	
	public Curso findById(IEmsRequest request){
		int id = request.getParamAsInt("id");
		return ModApplication.getInstance()
			.getCursoService()
			.findById(id);
	}
	
	public List<Curso> find(IEmsRequest request){
		String filter = request.getQuery("filter");
		String fields = request.getQuery("fields");
		int limit = request.getQueryAsInt("limit");
		int offset = request.getQueryAsInt("offset");
		String sort = request.getQuery("sort");
		return ModApplication.getInstance()
			.getCursoService()
			.find(filter, fields, limit, offset, sort);
	}

	public Curso insert(IEmsRequest request){
		final Curso curso = (Curso) request.getObject(Curso.class);
		return ModApplication.getInstance()
			.getCursoService()
			.insert(curso);
	}
	
	public Curso update(IEmsRequest request){
		int id = request.getParamAsInt("id");
		ModApplication app = ModApplication.getInstance();
		Curso curso = app.getCursoService().findById(id);
		request.mergeObjectFromPayload(curso);
		return app.getCursoService().update(curso);
	}
	
	public boolean delete(IEmsRequest request){
		final int id = request.getParamAsInt("id");
		return ModApplication.getInstance()
			.getCursoService()
			.delete(id);
	}
	
}
