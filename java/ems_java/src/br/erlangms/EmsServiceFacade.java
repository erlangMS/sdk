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
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;


/**
 * Classe de fachada para os serviços ErlangMS
 */
@Singleton
public abstract class EmsServiceFacade {
	private EmsConnection connection = null;
	private EmsConnection connectionSlave = null;

	@Resource
    private TimerService timerService = null;
	private TimerConfig destroyConnectionSlaveTimerConfig = null;
       	
    @PostConstruct
    public void initialize() {
        String className = getClass().getName();
    	connection = new EmsConnection(this, className, false);
        connection.start();
        try {
	        destroyConnectionSlaveTimerConfig = new TimerConfig("destroyConnectionSlave", false);
	        timerService.createCalendarTimer(new ScheduleExpression().minute("15").hour("*"), destroyConnectionSlaveTimerConfig);
        }catch(Exception e) {
        	System.out.println("Erro ao criar destroyConnectionSlaveTimerConfig para "+ getClass().getName());
    	}
    }
    
	@PreDestroy
    public void terminate() {
		connection.close();
		connection.interrupt();
		destroyConnectionSlave();
	}

	public void createConnectionSlave() {
        if (connectionSlave == null) {
			String className = getClass().getName();
			connectionSlave = new EmsConnection(this, className + "02", true);
	        connectionSlave.start();
	        System.out.println("Create slave connection to " + className);
        }
	}
	
	public void destroyConnectionSlave() {
		if (EmsUtil.properties.isLinux && connectionSlave != null) {
			connectionSlave.close();
			connectionSlave.interrupt();
			connectionSlave = null;
			System.out.println("Destroy slave connection to " + getClass().getName());
		}
	}
   
	@Timeout
    public void timeout(Timer timer) {
		// Somente libera o slave se não estiver fazendo nenhuma tarefa
		if (EmsUtil.properties.isLinux && connectionSlave != null && connectionSlave.getTaskCount() == 0) {
			destroyConnectionSlave();
		}
	}
	
	
}
