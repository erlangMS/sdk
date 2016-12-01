#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class ModApplication {
	private static ModApplication instance;
	public static ModApplication getInstance(){ return instance; }
	
	@EJB private CursoService cursoService;
	// Inclua outros EJBs aqui...
	
	
	public ModApplication(){
		instance = this;
	}
	
	public CursoService getCursoService() {
		return cursoService;
	}

}
