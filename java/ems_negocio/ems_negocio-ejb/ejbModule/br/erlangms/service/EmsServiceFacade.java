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
public abstract class EmsServiceFacade {
	private EmsAgent agent = null;
	private AgentThread agentThread = null;
	public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};
    private States state;
       	
    @PostConstruct
    public void initialize() {
        state = States.BEFORESTARTED;
        Class<? extends EmsServiceFacade> cls = getClass();
        agent = new EmsAgent(cls.getSimpleName(), cls.getName());
        agentThread = new AgentThread(agent);
        agentThread.start();
        state = States.STARTED;
    }
    
    @SuppressWarnings("deprecation")
	@PreDestroy
    public void terminate() {
        state = States.SHUTTINGDOWN;
        agentThread.stop();
        agent.close();
        agent = null;
        
    }

    public States getState() {
        return state;
    }
    
    public void setState(States state) {
        this.state = state;
    }    
    
	private class AgentThread extends Thread{
		private EmsAgent agent;
		
		public AgentThread(final EmsAgent agent){
			super();
			this.agent = agent;
		}
		
        @Override  
        public void run() {
        	try {
				agent.start();
			} catch (Exception e) {
				agent.print_log("Ocorreu o seguinte erro ao iniciar "+ agent.getNomeAgente() + ":");
				e.printStackTrace();
			}
        }  
	}
    
}
