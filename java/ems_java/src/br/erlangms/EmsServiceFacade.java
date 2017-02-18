/*********************************************************************
 * @title Módulo EmsServiceFacade
 * @version 1.0.0
 * @doc Classe de fachada para serviços ErlangMS
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * Classe de fachada para os serviços ErlangMS
 */
public abstract class EmsServiceFacade {
	private EmsConnection connection = null;
       	
    @PostConstruct
    public void initialize() {
        connection = new EmsConnection(this);
        connection.start();
    }
    
	@PreDestroy
    public void terminate() {
		connection.interrupt();
    }

    protected EmsConnection getConnection(){
    	return connection;
    }
   
}
