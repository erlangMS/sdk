package br.unb.unb_aula.infra;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.erlangms.EmsRepository;
import br.unb.unb_aula.model.Credito;

@Stateless
public class CreditoRepository extends EmsRepository<Credito> {

	@PersistenceContext(unitName = "service_context")
	public EntityManager serviceContext;

	@Override
	public EntityManager getEntityManager() {
		return serviceContext;
	}

	@Override
	public Class<Credito> getClassOfModel() {
		return Credito.class;
	}

}
