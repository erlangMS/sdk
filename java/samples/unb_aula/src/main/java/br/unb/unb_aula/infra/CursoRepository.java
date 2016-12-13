package br.unb.unb_aula.infra;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import br.erlangms.EmsRepository;
import br.unb.unb_aula.model.Curso;

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
