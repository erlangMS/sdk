#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.infra;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Startup
public class SaeInfra {
	private static SaeInfra instance;
	public static SaeInfra getInstance(){ return instance; }
 
	@EJB private DocumentacaoRepository documentacaoRepository;
	//@EJB inclua aqui outros EJBs...


	public SaeInfra(){
		instance = this;
	}
	
	

	@PersistenceContext(unitName = "service_context")
	public EntityManager saeContext;

	}

	public EntityManager getSaeContext() {
		return saeContext;
	}

	public DocumentacaoRepository getDocumentacaoRepository() {
		return documentacaoRepository;
	}


}
