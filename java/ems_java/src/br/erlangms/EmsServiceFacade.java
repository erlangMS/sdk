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
	private EmsConnection connection1 = null;
	private EmsConnection connection2 = null;
	private EmsConnection connection3 = null;
       	
    @PostConstruct
    public void initialize() {
        String className = getClass().getName();
    	connection = new EmsConnection(this, className);
        connection.start();
        connection1 = new EmsConnection(this, className + "01");
        connection1.start();
        connection2 = new EmsConnection(this, className + "02");
        connection2.start();
        connection3 = new EmsConnection(this, className + "03");
        connection3.start();
    }
    
	@PreDestroy
    public void terminate() {
		connection.close();
		connection.interrupt();
		connection1.close();
		connection1.interrupt();
		connection2.close();
		connection2.interrupt();
		connection3.close();
		connection3.interrupt();
	}

   
}
