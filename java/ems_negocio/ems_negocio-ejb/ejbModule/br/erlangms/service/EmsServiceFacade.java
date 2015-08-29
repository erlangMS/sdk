/*********************************************************************
 * @title Módulo EmsServiceFacade
 * @version 1.0.0
 * @doc Session Bean implementation class EmsServiceFacade
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import br.erlangms.EmsAgent;


/**
 * Session Bean implementation class EmsServiceFacade
 */
public abstract class EmsServiceFacade extends EmsAgent {
	public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};
    private States state;
       	
    public EmsServiceFacade() throws Exception {
    	super();
    }
    
    @PostConstruct
    public void initialize() throws Exception {
        state = States.BEFORESTARTED;
        start();
        state = States.STARTED;
        print_log("carregado!");
    }
    
    @PreDestroy
    public void terminate() {
        state = States.SHUTTINGDOWN;
        print_log("finalizado.");
    }

    public States getState() {
        return state;
    }
    
    public void setState(States state) {
        this.state = state;
    }    
}
