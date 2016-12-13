package br.unb.unb_aula.infra;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.erlangms.EmsRepository;
import br.unb.unb_aula.model.Pessoa;

@Stateless
public class PessoaRepository extends EmsRepository<Pessoa> {

	@PersistenceContext(unitName = "service_context")
	public EntityManager serviceContext;

	@Override
	public EntityManager getEntityManager() {
		return serviceContext;
	}

	@Override
	public Class<Pessoa> getClassOfModel() {
		return Pessoa.class;
	}

}
