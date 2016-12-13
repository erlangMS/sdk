package br.unb.unb_aula.infra;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Startup
public class ModInfra {
	private static ModInfra instance;
	public static ModInfra getInstance(){ return instance; }
 	
	@EJB private CursoRepository cursoRepository;
	@EJB private PessoaRepository pessoaRepository;
	@EJB private CreditoRepository creditoRepository;

	public ModInfra(){
		instance = this;
	}
	

	@PersistenceContext(unitName = "service_context")
	public EntityManager serviceContext;

	public EntityManager getServiceContext() {
		return serviceContext;
	}

	public CursoRepository getCursoRepository() {
		return cursoRepository;
	}

	public PessoaRepository getPessoaRepository() {
		return pessoaRepository;
	}

	public CreditoRepository getCreditoRepository() {
		return creditoRepository;
	}

	

}
