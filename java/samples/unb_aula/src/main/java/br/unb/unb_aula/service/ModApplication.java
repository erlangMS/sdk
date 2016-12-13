package br.unb.unb_aula.service;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class ModApplication {
	private static ModApplication instance;
	public static ModApplication getInstance(){ return instance; }
	
	@EJB private CursoService cursoService;
	@EJB private PessoaService pessoaService;
	// Inclua outros EJBs aqui...
	
	
	public ModApplication(){
		instance = this;
	}
	
	public CursoService getCursoService() {
		return cursoService;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}


	
}
