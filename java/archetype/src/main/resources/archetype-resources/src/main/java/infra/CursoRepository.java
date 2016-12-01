#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.infra;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import br.erlangms.EmsRepository;
import ${package}.model.Curso;

@Stateless
public class CursoRepository extends EmsRepository<Curso> {

	@PersistenceContext(unitName = "service_context")
	public EntityManager serviceContext;

	@Override
	public EntityManager getEntityManager() {
		return serviceContext;
	}

	@Override
	public Class<Curso> getClassOfModel() {
		return Curso.class;
	}

}
