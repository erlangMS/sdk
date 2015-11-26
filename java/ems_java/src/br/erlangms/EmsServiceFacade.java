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
public abstract class EmsServiceFacade implements IEmsServiceFacade {
	private EmsConnection connection = null;
	private DaemonThread daemon = null;
	public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};
    private States state;
       	
    @PostConstruct
    public void initialize() {
        state = States.BEFORESTARTED;
        Class<? extends EmsServiceFacade> cls = getClass();
        connection = new EmsConnection(cls.getSimpleName(), cls.getName(), this);
        daemon = new DaemonThread(connection);
        daemon.start();
        state = States.STARTED;
    }
    
    @SuppressWarnings("deprecation")
	@PreDestroy
    public void terminate() {
        state = States.SHUTTINGDOWN;
        daemon.stop();
        connection.close();
        connection = null;
        
    }

    protected States getState() {
        return state;
    }
    
    protected EmsConnection getConnection(){
    	return connection;
    }
    
	private class DaemonThread extends Thread{
		private EmsConnection agent;
		
		public DaemonThread(final EmsConnection agent){
			super();
			this.agent = agent;
		}
		
        @Override  
        public void run() {
        	try {
				agent.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }  
	}
    
}
