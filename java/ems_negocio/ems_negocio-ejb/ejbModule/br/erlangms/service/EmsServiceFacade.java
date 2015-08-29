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

import br.erlangms.EmsAgent;


/**
 * Classe base para serviços ErlangMS
 */
public abstract class EmsServiceFacade extends EmsAgent {
	public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};
    private States state;
       	
    @PostConstruct
    public void initialize() throws Exception {
        state = States.BEFORESTARTED;
        print_log("Carregando...");
        start();
        state = States.STARTED;
        print_log("Carregado!");
    }
    
    @PreDestroy
    public void terminate() {
        state = States.SHUTTINGDOWN;
        print_log("Finalizando...");
        close();
        print_log("Finalizado.");
    }

    public States getState() {
        return state;
    }
    
    public void setState(States state) {
        this.state = state;
    }    
}
