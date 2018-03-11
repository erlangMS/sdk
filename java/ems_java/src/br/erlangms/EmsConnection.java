/*********************************************************************
 * @title Módulo EmsConnection
 * @version 1.0.0
 * @doc Classe para conectar com o barramento ems_bus.
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

import br.erlangms.EmsUtil.EmsProperties;

public class EmsConnection extends Thread{
	public static final OtpErlangAtom ok_atom = new OtpErlangAtom("ok");
	private static final EmsProperties properties = EmsUtil.properties;
	private static final OtpErlangBinary result_ok = EmsUtil.result_ok; 
	private static final Logger logger = EmsUtil.logger;
	private static boolean erro_connection_epmd  = false;
	private static final int THREAD_WAIT_TO_RESTART = 5000;
	private static final String connectionErrorMessage = "Não foi possível realizar conexão ao barramento ERLANGMS. Verifique se o servidor de nome epmd foi iniciado.";
	private final String nameService;
	private final EmsServiceFacade facade;
	private Class<? extends EmsServiceFacade> classOfFacade;
	private Method methods[];
	private String method_names[];
	private int method_count = 0;
	private String otpNodeName;
	private OtpNode myNode;
	private OtpMbox myMbox;

	
	public EmsConnection(final EmsServiceFacade facade, final String otpNodeName){
		this.facade = facade;
		this.classOfFacade = facade.getClass();
		this.nameService = this.classOfFacade.getName();
		this.otpNodeName = otpNodeName.replace(".",  "_") + "_" + properties.nodeName;
		getMethodNamesTable();
	}
	
	/**
	 * Preenche uma tabela com os métodos do facade para acesso rápido pelo método chamaMetodo.
	 * Somente métodos com o parâmetro IEmsRequest.class. 
	 * @author Everton de Vargas Agilar
	 */
	private void getMethodNamesTable() {
		Method[] allMethods = classOfFacade.getMethods(); 
		methods = new Method[allMethods.length];
		method_names = new String[allMethods.length];
		for (Method m : allMethods){
			if (m.getParameterCount() == 1){
				Class<?> params[] = m.getParameterTypes();
				if (params[0] == IEmsRequest.class){
					methods[method_count] = m;
					method_names[method_count] = m.getName();
			    	m.setAccessible(true);  
					++method_count;
				}
			}
		}
	}

	public void close(){
		try{
			if (myMbox != null){
				myMbox.close();
			}
			if (myNode != null){
				myNode.close();
			}
		}catch (Exception e) {
			// Neste local é segudo não propagar exceptions
		}
	}
	
    @Override  
    public void run() {
		Random r = new Random();
        OtpErlangObject myObject;
        OtpErlangTuple myMsg;
        OtpErlangTuple otp_request;
    	OtpErlangPid dispatcherPid;
        EmsRequest request;
        StringBuilder msg_task = new StringBuilder();
        ExecutorService pool = Executors.newCachedThreadPool();
        boolean printInfo = true;
        boolean debug = EmsUtil.properties.debug;
        final String msgFinalizadoSucesso = nameService + " finalizado com sucesso.";
        final String msgReiniciarException = "Serviço "+ nameService + " será reiniado devido erro interno: ";
        while (true){
    		try {
	    		// Permanece neste loop até conseguir conexão com o barramento (EPMD deve estar ativo)
	    		while (true){
		    		try{
		    			myNode = new OtpNode(otpNodeName);
		    			break;
		    		}catch (IOException e){
		    			// Verifica se a thread não foi interrompida
		    			if (Thread.interrupted()) throw new InterruptedException();
		    			synchronized (e) {
			    			if (!erro_connection_epmd){
			    				erro_connection_epmd = true;
			    				logger.warning(connectionErrorMessage);
			    			}
						}
		    			try{
		    				// Aguarda um tempo aleatório até 10 segundos para conectar 
		    				Thread.sleep(3+r.nextInt(7));
		    			}catch (InterruptedException e1){
		    				// tenta novamente a comunicação se a thread não foi interrompida
		    				if (Thread.interrupted()) throw e1;
		    			}
		    		}
	    		}
	    	   
	    	   myNode.setCookie(properties.cookie);
	           myMbox = myNode.createMbox(nameService);
	           if (printInfo || debug){
	        	   logger.info(nameService + " node -> " + myNode.node());
	           }
	           
	           // Permanece neste loop aguardando mensagens do barramento
	           while(true){ 
	        	   try {
	                   if (Thread.interrupted()) throw new InterruptedException();
	                   request = new EmsRequest();
	                   msg_task.setLength(0);
	                   msg_task.append(this.otpNodeName).append(" [ ");
	                   myObject = myMbox.receive();
	                   myMsg = (OtpErlangTuple) myObject;
	                   otp_request = (OtpErlangTuple) myMsg.elementAt(0);
	                   request.setOtpRequest(otp_request);
	                   Long T2 = System.currentTimeMillis() - request.getT1();
	                   if (T2 > request.getTimeout() || (T2 > 6000 && request.isPostOrUpdateRequest())) {
	                	   logger.info("Serviço "+ nameService + " descartou mensagem tardia.");
	                	   continue;
	                   }
	                   dispatcherPid = (OtpErlangPid) myMsg.elementAt(1);
	                   myMbox.send(dispatcherPid, ok_atom);
                	   pool.submit(new Task(dispatcherPid, request, myMbox));
	                   msg_task.append(request.getMetodo()).append(" ")
	                   			.append(request.getFunction()).append(" RID: ")
	    						.append(request.getRID()).append("  Url: ")
	    						.append(request.getUrl())
	    						.append("  T2: ").append(Long.toString(T2))
	    						.append(" ]");
	                   logger.info(msg_task.toString());
	        	   } catch(final OtpErlangExit e3) {
	        		   // Somente sai do loop se a thead foi interrompida
	        		   if (Thread.interrupted()){
	        			   throw new InterruptedException();
	        		   }else{
		   	    			try{
			    				 Thread.sleep(THREAD_WAIT_TO_RESTART);
			    			}catch (InterruptedException e1){
			    				// volta ao trabalho  se a thread não foi interrompida
			    				if (Thread.interrupted()) throw e1;
			    			}
	        		   }
	    	       }
	           }
	    	} catch (final InterruptedException e2) {
	    		close();
	    		logger.info(msgFinalizadoSucesso);
	    		return;
	    	} catch (final Exception e) {
	    		// Qualquer outra exception não esperada o serviço será reiniciado
	    		logger.warning(msgReiniciarException + e.getMessage());
	    		e.printStackTrace();
	    		printInfo = true;
	    		close();
	    		try {
					Thread.sleep(THREAD_WAIT_TO_RESTART);
				} catch (InterruptedException e1) {
					if (Thread.interrupted()) return;
				}
			}
    	}
    }  
	

	/**
	 * Permite invocar o método referente um web Service e realizar todo o tratamento necessário.
	 * @author Everton de Vargas Agilar
	 */
    private Object chamaMetodo(final String modulo, final String metodo, final IEmsRequest request)  {
    	Method m = null;
    	Object result = null;
		String msg_json = null;
		try {  
    		// localiza o método na tabela de métodos
    		for (int i = 0; i < method_count; i++){
    			if (metodo.equals(method_names[i])){
    				m = methods[i];
    				break;
    			}
    		}
    		
    		// se não encontrou o método tenta invocar sem parâmetros (não otimizado)
    		if (m == null){
	    		m = classOfFacade.getMethod(metodo);
		    	m.setAccessible(true);  
    		}

    		// invoca o método
    		if (m.getReturnType().getName().equals("void")){
		    	m.invoke(facade, request);
		    	return result_ok;
		    }else{
		    	result = m.invoke(facade, request);
		    }

    		return result;
		} catch (final NoSuchMethodException e) {  
	        // Essa exceção ocorre se o getMethod() não encontrar o método
	    	String erro = "Método da camada de serviço não encontrado: " + metodo + ".";
	    	msg_json = "{\"error\":\"validation\", \"message\" : \"" + erro + "\"}"; 
	    	logger.info(erro);
	    	return new EmsResponse(400, msg_json); 
	    } catch (final IllegalAccessException e) {  
	        // Pode ocorrer se o método que você está invocando não for  
	        // acessível. Você pode forçar que um método (mesmo privado!) seja  
	        // acessível fazendo:  
	        // antes do seu invoke.
	    	String erro = "Acesso ilegal ao método da camada de serviço: " + metodo + ".";
	    	msg_json = "{\"error\":\"validation\", \"message\" : \"" + erro + "\"}"; 
	    	logger.info(erro);
	    	return new EmsResponse(400, msg_json); 
	    } catch (final InvocationTargetException e) {  
	        // Essa exceção acontece se o método chamado gerar uma exceção.  
	        // Use e.getCause() para descobrir qual exceção foi gerada no método  
	        // chamado e trata-la adequadamente.
	    	Throwable cause = e.getCause();
	    	if (cause != null){
		    	if (cause instanceof EmsValidationException){
		    		List<String> errors = ((EmsValidationException)cause).getErrors();
		    		String msg = null;
			    	if (errors.size() > 1){
			    		msg = EmsUtil.toJson(errors);
			    		msg_json = "{\"error\":\"validation\", \"message\" : " + msg + "}";
			    	}else if (errors.size() == 1){
			    		msg = EmsUtil.toJson(errors.get(0));
			    		msg_json = "{\"error\":\"validation\", \"message\" : " + msg + "}";
			    	}else{
			    		msg_json = "{\"error\":\"validation\", \"message\" : \"\"}";
			    	}
			    	return new EmsResponse(400, msg_json);
		    	}else if (cause instanceof EmsNotFoundException){
		    		msg_json = "{\"error\":\"enoent\", \"message\" : " + EmsUtil.toJson(cause.getMessage()) + "}";
		    		return new EmsResponse(404, msg_json);
		    	}else if (cause instanceof javax.ejb.EJBException){
		    		try{
			    		Exception causeEx = ((javax.ejb.EJBException) cause).getCausedByException();
			    		if (causeEx != null){
				    		if (causeEx.getCause() != null){
				    			cause = causeEx.getCause();
				    			if (causeEx.getCause().getCause() != null){
					    			cause = causeEx.getCause().getCause();
				    			}
				    		}
			    		}
			    		String motivo = null;
			    		int posMsgSql = cause.getMessage().toLowerCase().indexOf("unique index");
		    			if (posMsgSql != -1){
		    				motivo = "Registro duplicado, verifique.";
		    			}else{
				    		posMsgSql = cause.getMessage().indexOf("; SQL statement:");
		    				if (posMsgSql > 0){
				    			motivo = cause.getMessage().substring(0, posMsgSql-1);
				    		}else{
			    				motivo = cause.getMessage();	
				    		}
		    			}
			    		msg_json = "{\"error\":\"validation\", \"message\" : " + EmsUtil.toJson(motivo) + "}";
			    		return new EmsResponse(400, msg_json);
		    		}catch (final Exception ex){
				    	String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + e.getCause() + "."; 
				    	msg_json = "{\"error\":\"validation\", \"message\" : \"" + erro + "\"}"; 
				    	logger.info(erro);
				    	return new EmsResponse(400, msg_json); 
		    		}
		    	}else{
		    		Throwable target = e.getTargetException();
	    			msg_json = "{\"error\":\"validation\", \"message\" : \"Requisição inválida.\"}";
		    		if (target != null){
		    			cause = e.getCause();
		    			if (cause != null){
	    	    			String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + cause.getMessage() + ".";
		    	    		logger.info(erro);
		    	    		return new EmsResponse(404, msg_json);
		    			}else{
		    				String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + e.getMessage() + ".";
		    				logger.info(erro);
		    				return new EmsResponse(404, msg_json);
		    			}
		    		}else{
    	    			String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + e.getMessage() + ".";
	    	    		logger.info(erro);
	    	    		return new EmsResponse(404, msg_json);
		    		}
		    	}
	    	}else{
		    	String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + e.getCause() + "."; 
		    	msg_json = "{\"error\":\"validation\", \"message\" : \"Requisição inválida.\"}";
		    	logger.info(erro);
		    	return new EmsResponse(400, msg_json);
	    	}
	    }
	}  	
	
	/**
	 * Classe interna para realizar o trabalho enquanto o loop principal do serviço  aguarda mensagens.
	 * @author Everton de Vargas Agilar
	 */
	private final class Task implements Callable<Boolean>{
		private OtpErlangPid from;
		private IEmsRequest request;
		private OtpMbox myMbox;
		
		public Task(final OtpErlangPid from, final IEmsRequest request, final OtpMbox myMbox){
			super();
			this.from = from;
			this.request = request;
			this.myMbox = myMbox;
		}
		
        public Boolean call() {  
        	Object ret = chamaMetodo(request.getModulo(), request.getFunction(), request);
        	OtpErlangTuple response = EmsUtil.serializeObjectToErlangResponse(ret, request);
        	myMbox.send(from, response);
			return true;
        }  
	}

	
}
