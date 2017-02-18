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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

import br.erlangms.EmsUtil.EmsProperties;

public class EmsConnection extends Thread{
	private static EmsProperties properties;
	private String otpNodeName;
	private final String nomeAgente;
	private final String nomeService;
	private static final OtpErlangBinary result_ok; 
	private static final Logger logger;
	private final EmsServiceFacade facade;
	private Class<? extends EmsServiceFacade> classOfFacade;
	private static OtpErlangPid dispatcherPid;
	private Method methods[];
	private String method_names[];
	private int method_count = 0;
	static{
		properties = EmsUtil.properties;
		logger = Logger.getLogger("erlangms");
		result_ok = new OtpErlangBinary("{\"ok\":\"ok\"}".getBytes());
    }
    
	public EmsConnection( final EmsServiceFacade facade){
		this.facade = facade;
		this.classOfFacade = facade.getClass();
		this.nomeAgente = classOfFacade.getSimpleName();
		this.nomeService = classOfFacade.getName();
		this.otpNodeName = nomeAgente + "_" + properties.nodeName;
		getMethodNamesTable();
	}
	
	/**
	 * Preenche uma tabela com os métodos do facade para acesso rápido pelo método chamaMetodo 
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

    @Override  
    public void run() {
    	try {
    		OtpNode myNode = null;
    		OtpMbox myMbox = null;
    		
    		// Fica nesse loop até consegui conexão com o barramento
    		while (true){
	    		try{
	    			myNode = new OtpNode(otpNodeName);
	    			break;
	    		}catch (IOException e){
	    			// Verifica se a thread não foi interrompida
	    			if (Thread.interrupted()) throw new InterruptedException();
	    			logger.warning("Não foi possível se conectar ao barramento ERLANGMS. Verifique se o servidor de nome epmd está iniciado.");
	    			try{
	    				 Thread.sleep(10000);
	    			}catch (InterruptedException e1){
	    				// tenta novamente a comunicação se a thread não foi interrompida
	    				if (Thread.interrupted()) throw e1;
	    			}
	    		}
    		}
    	   
    	   myNode.setCookie(properties.cookie);
           StringBuilder msg_node = new StringBuilder(nomeService)
    										.append(" node -> ").append(myNode.node())
    										.append(" port -> ").append(myNode.port());
           logger.info(msg_node.toString());
           myMbox = myNode.createMbox(nomeService);
           OtpErlangObject myObject;
           OtpErlangTuple myMsg;
           OtpErlangTuple otp_request;
           IEmsRequest request;
           StringBuilder msg_task = new StringBuilder();
           ExecutorService pool = Executors.newFixedThreadPool(properties.maxThreadPool);
           
           // Fica nesse loop aguardando mensagens do barramento
           while(true){ 
        	   try {
                   if (Thread.interrupted()) throw new InterruptedException();
        		   myObject = myMbox.receive();
                    myMsg = (OtpErlangTuple) myObject;
                    otp_request = (OtpErlangTuple) myMsg.elementAt(0);
                    request = new EmsRequest(otp_request);
                    dispatcherPid = (OtpErlangPid) myMsg.elementAt(1);
                    msg_task.setLength(0);
                    msg_task.append(request.getMetodo()).append(" ").append(request.getModulo())
    						.append(".").append(request.getFunction()).append(" [RID: ")
    						.append(request.getRID()).append(", ").append(request.getUrl()).append("]");
                    logger.info(msg_task.toString());
                    
                    // Delega o trabalho para um worker
                    pool.submit(new Task(dispatcherPid, request, myMbox));
        	   
        	   } catch(OtpErlangExit e3) {
        		   if (Thread.interrupted()) throw new InterruptedException();
    	       }
           }
    	} catch (InterruptedException e2) {
    		logger.info(nomeService + " finalizado com sucesso.");
    	} catch (Exception e) {
    		logger.warning(nomeService + " finalizado com erro. Motivo: "+ e.getMessage());
		}
    }  
	

	/**
	 * Permite invocar o método referente um web-service e realizar todo o tratamento necessário.
	 * @author Everton de Vargas Agilar
	 */
    private Object chamaMetodo(final String modulo, final String metodo, final IEmsRequest request)  {
    	Method m = null;
    	Object result = null;
		String msg_json = null;
		try {  
    		// localiza o método 
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
		} catch (NoSuchMethodException e) {  
	        // Essa exceção ocorre se o getMethod() não encontrar o método
	    	String erro = "Método da camada de serviço não encontrado: " + metodo + ".";
	    	msg_json = "{\"error\":\"validation\", \"message\" : \"" + erro + "\"}"; 
	    	logger.info(erro);
	    	return new EmsResponse(400, msg_json); 
	    } catch (IllegalAccessException e) {  
	        // Pode ocorrer se o método que você está invocando não for  
	        // acessível. Você pode forçar que um método (mesmo privado!) seja  
	        // acessível fazendo:  
	        // antes do seu invoke.
	    	String erro = "Acesso ilegal ao método da camada de serviço: " + metodo + ".";
	    	msg_json = "{\"error\":\"validation\", \"message\" : \"" + erro + "\"}"; 
	    	logger.info(erro);
	    	return new EmsResponse(400, msg_json); 
	    } catch (InvocationTargetException e) {  
	        // Essa exceção acontece se o método chamado gerar uma exceção.  
	        // Use e.getCause() para descobrir qual exceção foi gerada no método  
	        // chamado e trata-la adequadamente.
	    	Throwable cause = e.getCause();
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
		    		cause = causeEx.getCause().getCause();
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
	    		}catch (Exception ex){
			    	String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + e.getCause() + "."; 
			    	msg_json = "{\"error\":\"validation\", \"message\" : \"" + erro + "\"}"; 
			    	logger.info(erro);
			    	return new EmsResponse(400, msg_json); 
	    		}
	    	}else{
		    	String erro = "O método "+ modulo + "." + metodo + " gerou um erro: " + e.getCause() + "."; 
		    	msg_json = "{\"error\":\"validation\", \"message\" : " + EmsUtil.toJson(erro) + "}";
		    	logger.info(erro);
		    	return new EmsResponse(400, msg_json);
	    	}
	    }
	}  	
	
	/**
	 * Classe interna para realizar o trabalho enquanto o loop principal do web-service aguarda mensagens.
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
