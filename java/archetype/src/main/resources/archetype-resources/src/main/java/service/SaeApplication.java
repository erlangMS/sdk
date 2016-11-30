#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class SaeApplication {
	private static SaeApplication instance;
	public static SaeApplication getInstance(){ return instance; }
	
	@EJB private DocumentacaoService documentacaoService;
	// Inclusa outros EJBs aqui...
	
	
	public SaeApplication(){
		instance = this;
	}
	
	public DocumentacaoService getDocumentacaoService() {
		return documentacaoService;
	}

}
