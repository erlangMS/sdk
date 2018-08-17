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
import javax.ejb.Singleton;


/**
 * Classe de fachada para os serviços ErlangMS
 */
@Singleton
public abstract class EmsServiceFacade {
	private EmsConnection connection = null;
	private EmsConnection connectionSlave = null;

    @PostConstruct
    public void initialize() {
        String className = getClass().getName();
    	connection = new EmsConnection(this, className, false);
        connection.start();
		if (EmsUtil.properties.isLinux) {
			connectionSlave = new EmsConnection(this, className + "02", true);
			connectionSlave.start();
		}
    }
    
	@PreDestroy
    public void terminate() {
		connection.close();
		connection.interrupt();
		if (EmsUtil.properties.isLinux) {
			connectionSlave.close();
			connectionSlave.interrupt();
		}
	}
}
