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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

public class EmsAgent
{
    private final OtpErlangAtom ok = new OtpErlangAtom("ok");
    private final OtpErlangAtom servico_atom = new OtpErlangAtom("servico");
	private final OtpErlangBinary result_ok = new OtpErlangBinary("{\"ok\":\"ok\"}".getBytes());
	private final OtpErlangBinary result_null = new OtpErlangBinary("{\"ok\":\"null\"}".getBytes());
	private final OtpErlangBinary erro_convert_json = new OtpErlangBinary("{\"erro\":\"service_exception\", \"message\" : \"Falha na serialização do conteúdo em JSON\"}".getBytes());
    private IEmsServiceFacade facade = null;
	private String nomeAgente = null;
	private String nomeService = null;
	private OtpNode myNode = null;
    private static Logger logger = Logger.getLogger(EmsAgent.class);
    
	public EmsAgent(final String nomeAgente, final String nomeService, final IEmsServiceFacade facade){
		this.nomeAgente = nomeAgente;
		this.nomeService = nomeService;
		this.facade = facade;
	}
	
	public String getNomeAgente(){
		return nomeAgente;
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
	       OtpMbox myMbox = myNode.createMbox(nomeService);
	       OtpErlangObject myObject;
           OtpErlangTuple myMsg;
           OtpErlangPid from;
           OtpErlangTuple otp_request;
           IEmsRequest request;
           StringBuilder msg_task = new StringBuilder();
           while(true) 
	    	   try {
                    myObject = myMbox.receive();
                    myMsg = (OtpErlangTuple) myObject;
                    otp_request = (OtpErlangTuple) myMsg.elementAt(0);
                    request = new EmsRequest(otp_request);
                    from = (OtpErlangPid) myMsg.elementAt(1);
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
                    new Task(from, request, myMbox).start();  
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
	    	return "{\"erro\":\"service_exception\", \"message\" : \"" + erro + "\"}";
	    } catch (IllegalAccessException e) {  
	        // Pode ocorrer se o método que você está invocando não for  
	        // acessível. Você pode forçar que um método (mesmo privado!) seja  
	        // acessível fazendo:  
	        // antes do seu invoke.
	    	String erro = "Acesso ilegal ao método de negócio: " + metodo + ".";
	    	print_log(erro);
	    	return "{\"erro\":\"service_exception\", \"message\" : \"" + erro + "\"}";
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
	    		msg_json = "{\"erro\":\"request\", \"message\" : " + EmsUtil.toJson(cause.getMessage()) + "}";
	    		return msg_json;
	    	}else if (cause instanceof EmsNotFoundException){
	    		msg_json = "{\"erro\":\"notfound\", \"message\" : " + EmsUtil.toJson(cause.getMessage()) + "}";
	    		return msg_json;
	    	}else if (cause instanceof javax.ejb.EJBTransactionRolledbackException){
	    		try{
		    		Exception causeEx = ((javax.ejb.EJBTransactionRolledbackException) cause).getCausedByException();
		    		cause = causeEx.getCause().getCause();
		    		String motivo = null;
		    		int posMsgSql = cause.getMessage().indexOf("; SQL statement:");
		    		if (posMsgSql > 0){
		    			motivo = cause.getMessage().substring(0, posMsgSql-1);
		    		}else{
		    			motivo = cause.getMessage();
		    		}
		    		msg_json = "{\"erro\":\"ejb\", \"message\" : " + EmsUtil.toJson(motivo) + "}";
		    		return msg_json;
	    		}catch (Exception ex){
			    	String erro = "O método "+ modulo + "." + metodo + " gerou uma excessão: " + e.getCause() + "."; 
			    	print_log(erro);
			    	return "{\"erro\":\"service_exception\", \"message\" : \"" + erro + "\"}";
	    		}
	    	}else{
		    	String erro = "O método "+ modulo + "." + metodo + " gerou uma excessão: " + e.getCause() + "."; 
		    	print_log(erro);
		    	return "{\"erro\":\"service_exception\", \"message\" : " + EmsUtil.toJson(erro) + "}";
	    	}
	    }
	}  	
	
	public void print_log(final String message){
		logger.info(new StringBuilder(nomeAgente).append(": ").append(message).toString());
	}

	private final class Task extends Thread{
		private OtpErlangPid from;
		private IEmsRequest request;
		private OtpMbox myMbox;
		
		public Task(final OtpErlangPid from, final IEmsRequest request, final OtpMbox myMbox){
			super();
			this.from = from;
			this.request = request;
			this.myMbox = myMbox;
		}
		
        @Override  
        public void run() {  
            OtpErlangObject[] otp_result = new OtpErlangObject[3];
        	Object ret = chamaMetodo(request.getModulo(), request.getFunction(), request);
            OtpErlangObject[] reply = new OtpErlangObject[2];
            reply[0] = ok;
            if (ret != null){
            	try{
	            	String m_json = null;
	            	if (ret instanceof OtpErlangAtom){
	            		reply[1] = (OtpErlangObject) ret;
	            	}else if (ret instanceof Integer || ret instanceof Boolean){
	            		m_json = "{\"ok\":"+ ret.toString() + "}";
	            		reply[1] = new OtpErlangBinary(m_json.getBytes());
	    	    	}else if (ret instanceof java.util.Date || 
	  	    			  	  ret instanceof java.sql.Timestamp ||
	  	    			  	  ret instanceof Double){
	    	    		m_json = "{\"ok\":"+ EmsUtil.toJson(ret) + "}";
	    	    		reply[1] = new OtpErlangBinary(m_json.getBytes());
	            	}else if (ret instanceof String){
		            	reply[1] = new OtpErlangBinary(((String) ret).getBytes());
		            }else if (ret instanceof Object){
		            	reply[1] = new OtpErlangBinary(EmsUtil.toJson(ret).getBytes());
		            }else if (ret.getClass().getName().equals(ArrayList.class.getName())){
		            	List<?> lista = (List<?>) ret;
		            	OtpErlangObject[] otp_items = new OtpErlangObject[lista.size()];
		            	for(int i = 0; i < lista.size(); i++){
		            		otp_items[i] = new OtpErlangString((String) lista.get(i));
		            	}
		            	OtpErlangList otp_list = new OtpErlangList(otp_items);
		            	reply[1] = otp_list;
		            }
            	}catch (Exception e){
            		reply[1] = erro_convert_json;
            	}
            }else{
        		reply[1] = result_null;
            }
            otp_result[0] = servico_atom;
            otp_result[1] = new OtpErlangLong(request.getRID());
            otp_result[2] = new OtpErlangTuple(reply);
            OtpErlangTuple myTuple = new OtpErlangTuple(otp_result);
            myMbox.send(from, myTuple);
        }  
	}
	
}
