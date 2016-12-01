#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import java.util.List;
import javax.ejb.Stateless;
import ${package}.infra.ModInfra;
import ${package}.model.Curso;

@Stateless
public class CursoService {

	public Curso findById(Integer id) {
		return ModInfra.getInstance()
			.getCursoRepository()
			.findById(id);
	}

	public List<Curso> find(String filtro, String fields, int limit_ini, int limit_fim, String sort) {
		return ModInfra.getInstance()
			.getCursoRepository()
			.find(filtro, fields, limit_ini, limit_fim, sort);
	}

	public Curso update(Curso curso){
		curso.validar();
		return ModInfra.getInstance()
			.getCursoRepository()
			.update(curso);
	}

	public Curso insert(Curso curso) {
		curso.validar();
		return ModInfra.getInstance()
			.getCursoRepository()
			.insert(curso);
	}
	
	public boolean delete(Integer id) {
		return ModInfra.getInstance()
			.getCursoRepository()
			.delete(id);
	}
	
}
