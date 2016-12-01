#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.facade;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;
import ${package}.model.Curso;
import ${package}.service.ModApplication;
 
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
		String filtro = request.getQuery("filtro");
		String fields = request.getQuery("fields");
		int limit_ini = request.getQueryAsInt("limit_ini");
		int limit_fim = request.getQueryAsInt("limit_fim");
		String sort = request.getQuery("sort");
		return ModApplication.getInstance()
			.getCursoService()
			.find(filtro, fields, limit_ini, limit_fim, sort);
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
