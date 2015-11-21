/*********************************************************************
 * @title Módulo EmsAgent
 * @version 1.0.0
 * @doc Agente que gerencia o ErlangMS MailBox 
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

public class EmsAgent
{
	private static final int MAX_THREAD_POOL_BY_AGENT = 4;
	private static final OtpErlangBinary result_ok = new OtpErlangBinary("{\"ok\":\"ok\"}".getBytes());
	private static Logger logger = Logger.getLogger(EmsAgent.class);
	private final IEmsServiceFacade facade;
	private final String nomeAgente;
	private final String nomeService;
	private OtpNode myNode = null;
	private OtpMbox myMbox = null;
    private static OtpErlangPid dispatcherPid;

    
	public EmsAgent(final String nomeAgente, final String nomeService, final IEmsServiceFacade facade){
		this.nomeAgente = nomeAgente;
		this.nomeService = nomeService;
		this.facade = facade;
	}
	
	public String getNomeAgente(){
		return nomeAgente;
	}

	public OtpNode getNode(){
		return myNode;
	}
	
	public OtpMbox getMBox(){
		return myMbox;
	}

	public static OtpErlangPid getDispatcherPid(){
		return dispatcherPid;
	}
	
	public void start() throws Exception {
	   // Se existir conexão previa, finaliza primeiro
	   if (myNode != null){
		   close(); 
	   }
	   myNode = new OtpNode(nomeAgente);
       myNode.setCookie("erlangms");
       StringBuilder msg_node = new StringBuilder(nomeService)
										.append(" host -> ").append(myNode.host())
										.append(" node -> ").append(myNode.node())
										.append(" port -> ").append(myNode.port())
										.append(" cookie -> ").append(myNode.cookie());
       print_log(msg_node.toString());
       myMbox = myNode.createMbox(nomeService);
       myMbox.registerName(nomeService);
	   OtpErlangPid xx = myNode.whereis(nomeService);
	   System.out.println(xx.toString());
       OtpErlangObject myObject;
       OtpErlangTuple myMsg;
       OtpErlangTuple otp_request;
       IEmsRequest request;
       StringBuilder msg_task = new StringBuilder();
       ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_POOL_BY_AGENT);
       while(true) 
    	   try {
                myObject = myMbox.receive();
                myMsg = (OtpErlangTuple) myObject;
                otp_request = (OtpErlangTuple) myMsg.elementAt(0);
                request = new EmsRequest(otp_request);
                dispatcherPid = (OtpErlangPid) myMsg.elementAt(1);
                msg_task.setLength(0);
                msg_task.append(request.getMetodo())
						.append(" ")
						.append(request.getModulo())
						.append(".")
						.append(request.getFunction())
						.append(" [RID: ")
						.append(request.getRID())
						.append(", ")
						.append(request.getUrl())
						.append("]");
                print_log(msg_task.toString());
                pool.submit(new Task(dispatcherPid, request, myMbox));
		} catch(OtpErlangExit e) {
			break;
        }
    }
	
	public void close(){
		if (myNode != null){
			try{
				myNode.close();
			}catch (Exception e){
				print_log("Ocorreu o seguinte erro ao finalizar: ");
				e.printStackTrace();
			}finally{
				print_log(new StringBuilder("EmsAgent para ").append(nomeService).append(" finalizado.").toString());
				myNode = null;
			}
		}
	}
	
	private Object chamaMetodo(final String modulo, final String metodo, final IEmsRequest request)  {
    	Method m = null;
    	Object result = null;
		String msg_json = null;
		try {  
	    	Class<?> Classe = facade.getClass();
	    	try{
	    		m = Classe.getMethod(metodo, IEmsRequest.class);   
		    	m.setAccessible(true);  
			    if (m.getReturnType().getName().equals("void")){
			    	m.invoke(facade, request);
			    	return result_ok;
			    }else{
			    	result = m.invoke(facade, request);
			    }
	    	} catch (NoSuchMethodException e) {
	    		m = Classe.getMethod(metodo);
		    	m.setAccessible(true);  
			    if (m.getReturnType().getName().equals("void")){
			    	m.invoke(facade);
			    	return result_ok;
			    }else{
			    	result = m.invoke(facade);
			    }
	    	}
	    	return result;
		} catch (NoSuchMethodException e) {  
	        // Essa exceção ocorre se o getMethod() não encontrar o método
	    	String erro = "Método de negócio não encontrado: " + metodo + ".";
	    	print_log(erro);
	    	return "{\"erro\":\"service\", \"message\" : \"" + erro + "\"}";
	    } catch (IllegalAccessException e) {  
	        // Pode ocorrer se o método que você está invocando não for  
	        // acessível. Você pode forçar que um método (mesmo privado!) seja  
	        // acessível fazendo:  
	        // antes do seu invoke.
	    	String erro = "Acesso ilegal ao método de negócio: " + metodo + ".";
	    	print_log(erro);
	    	return "{\"erro\":\"service\", \"message\" : \"" + erro + "\"}";
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
		    		msg_json = "{\"erro\":\"validation\", \"message\" : " + msg + "}";
		    	}else if (errors.size() == 1){
		    		msg = EmsUtil.toJson(errors.get(0));
		    		msg_json = "{\"erro\":\"validation\", \"message\" : " + msg + "}";
		    	}else{
		    		msg_json = "{\"erro\":\"validation\", \"message\" : \"\"}";
		    	}
		    	return msg_json;
	    	}else if (cause instanceof EmsRequestException){
	    		msg_json = "{\"erro\":\"facade\", \"message\" : " + EmsUtil.toJson(cause.getMessage()) + "}";
	    		return msg_json;
	    	}else if (cause instanceof EmsNotFoundException){
	    		msg_json = "{\"erro\":\"notfound\", \"message\" : " + EmsUtil.toJson(cause.getMessage()) + "}";
	    		return msg_json;
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
		    		msg_json = "{\"erro\":\"service\", \"message\" : " + EmsUtil.toJson(motivo) + "}";
		    		return msg_json;
	    		}catch (Exception ex){
			    	String erro = "O método "+ modulo + "." + metodo + " gerou uma excessão: " + e.getCause() + "."; 
			    	print_log(erro);
			    	return "{\"erro\":\"service\", \"message\" : \"" + erro + "\"}";
	    		}
	    	}else{
		    	String erro = "O método "+ modulo + "." + metodo + " gerou uma excessão: " + e.getCause() + "."; 
		    	print_log(erro);
		    	return "{\"erro\":\"service\", \"message\" : " + EmsUtil.toJson(erro) + "}";
	    	}
	    }
	}  	
	
	public void print_log(final String message){
		logger.info(new StringBuilder(nomeAgente).append(": ").append(message).toString());
	}

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
        	OtpErlangTuple response = EmsUtil.serializeObjectToErlangResponse(ret, request.getRID());
        	myMbox.send(from, response);
			return true;
        }  
	}
	
}
