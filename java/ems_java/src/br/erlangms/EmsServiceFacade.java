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
	private EmsConnection connection4 = null;
	private EmsConnection connection5 = null;
       	
    @PostConstruct
    public void initialize() {
        String className = getClass().getName();
    	connection = new EmsConnection(this, className);
        connection.start();
        if (EmsUtil.properties.isLinux) {
	        connection1 = new EmsConnection(this, className + "01");
	        connection1.start();
	        connection2 = new EmsConnection(this, className + "02");
	        connection2.start();
	        connection3 = new EmsConnection(this, className + "03");
	        connection3.start();
	        connection4 = new EmsConnection(this, className + "04");
	        connection4.start();
	        connection5 = new EmsConnection(this, className + "05");
	        connection5.start();
        }
    }
    
	@PreDestroy
    public void terminate() {
		connection.close();
		connection.interrupt();
		if (EmsUtil.properties.isLinux) {
			connection1.close();
			connection1.interrupt();
			connection2.close();
			connection2.interrupt();
			connection3.close();
			connection3.interrupt();
			connection4.close();
			connection4.interrupt();
			connection5.close();
			connection5.interrupt();
		}
	}

   
}
