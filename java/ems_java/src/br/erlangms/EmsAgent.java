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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangInt;
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
    private final OtpErlangAtom null_atom = new OtpErlangAtom("null");
	private IEmsServiceFacade facade = null;
	private String nomeAgente = null;
	private String nomeService = null;
    private OtpNode myNode = null;
    private static Logger logger = Logger.getLogger(EmsAgent.class);
    
	public EmsAgent(final String nomeAgente, final String nomeService, IEmsServiceFacade facade){
		BasicConfigurator.configure();
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
			   print_log("Já existe EmsAgent para "+ nomeService + ", finalizando primeiro...");
			   close(); 
		   }
		   print_log("EmsAgent para " + nomeService + " iniciado.");
	       myNode = new OtpNode(nomeAgente);
	       myNode.setCookie("erlangms");
	       print_log("host   -> "+ myNode.host());
	       print_log("node   -> "+ myNode.node());
	       print_log("port   -> "+ myNode.port());
	       print_log("cookie -> "+ myNode.cookie());
	       OtpMbox myMbox = myNode.createMbox(nomeService);
	       OtpErlangObject myObject;
           OtpErlangTuple myMsg;
           OtpErlangPid from;
           OtpErlangTuple otp_request;
           IEmsRequest request;
           print_log("EmsAgent [OK]");
           while(true) 
	    	   try {
                    myObject = myMbox.receive();
                    myMsg = (OtpErlangTuple) myObject;
                    otp_request = (OtpErlangTuple) myMsg.elementAt(0);
                    request = new EmsRequest(otp_request);
                    from = (OtpErlangPid) myMsg.elementAt(1);
                    print_log(request.getMetodo() + " request " + request.getRID() + " para " + request.getModulo() + "." + request.getFunction() +  "(EmsRequest) [URL: " + request.getUrl()+ "]");
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
				print_log("EmsAgent para " + nomeService + " finalizado.");
				myNode = null;
			}
		}
	}
	
	private Object chamaMetodo(final String modulo, final String metodo, final IEmsRequest request)  {
	    try {  
	    	Class<?> Classe = facade.getClass();
	    	Method m = null;
	    	Object result = null;
	    	try{
	    		m = Classe.getMethod(metodo, IEmsRequest.class);   
		    	m.setAccessible(true);  
			    result = m.invoke(facade, request);          
	    	} catch (NoSuchMethodException e) {
		    	m = Classe.getMethod(metodo);
		    	m.setAccessible(true);  
			    result = m.invoke(facade);          
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
	    	String erro = "O método "+ modulo + "." + metodo + " gerou uma excessão: " + e.getCause() + "."; 
	    	print_log(erro);
	    	return "{\"erro\":\"service_exception\", \"message\" : \"" + erro + "\"}";
	    }
	}  	
	
	public void print_log(final String message){
		logger.info(nomeAgente + ": " + message);
	}

	private final class Task extends Thread{
		private OtpErlangPid from;
		private IEmsRequest request;
		private OtpMbox myMbox;
		
		public Task(final OtpErlangPid from, IEmsRequest request, OtpMbox myMbox){
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
	            if (ret.getClass().getName().equals(Integer.class.getName())){
	            	reply[1] = new OtpErlangInt((Integer) ret);
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
	            }else if (ret instanceof OtpErlangAtom){
	            	reply[1] = (OtpErlangObject) ret;
	            }
            }else{
            	reply[1] = (OtpErlangObject) null_atom;
            }
            
            otp_result[0] = servico_atom;
            otp_result[1] = new OtpErlangLong(request.getRID());
            otp_result[2] = new OtpErlangTuple(reply);
            OtpErlangTuple myTuple = new OtpErlangTuple(otp_result);
            
            myMbox.send(from, myTuple);
        }  
	}
	
}
