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

import com.ericsson.otp.erlang.OtpErlangAtom;
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
import com.google.gson.Gson;

public class EmsAgent
{
	private String nomeAgente = null;
	private String nomeService = null;
    private OtpNode myNode = null;
    private final OtpErlangAtom ok = new OtpErlangAtom("ok");
    private final OtpErlangAtom service_not_implemented = new OtpErlangAtom("service_not_implemented");
    private final OtpErlangAtom metodo_not_implemented = new OtpErlangAtom("metodo_not_implemented");
    private final OtpErlangAtom metodo_not_visible = new OtpErlangAtom("metodo_not_visible");
    private final OtpErlangAtom negocio_exception = new OtpErlangAtom("negocio_exception");
    private final OtpErlangAtom servico = new OtpErlangAtom("servico");
    
	public static void main(String[] args) throws Exception {
		new EmsAgent("MainAgenteTest", "br.erlangMS.MainAgentTest").start();
	}
	
	public EmsAgent(final String nomeAgente, final String nomeService){
		this.nomeAgente = nomeAgente;
		this.nomeService = nomeService;
	}
	
	public String getNomeAgente(){
		return nomeAgente;
	}
	
	public void start() throws Exception {
		   // Se existir conexão previa, finaliza primeiro
		   if (myNode != null){
			   print_log("Já existe EmsAgent para "+ nomeAgente + ", finalizando primeiro...");
			   close(); 
		   }
		   print_log("EmsAgent para " + nomeAgente + " iniciado.");
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
				print_log("EmsAgent para " + nomeAgente + " finalizado.");
				myNode = null;
			}
		}
	}
	
	static public String toJson(final Object object){
		Gson gson = new Gson();
		String result = gson.toJson(object);
		return result;
	}
	
	private Object chamaMetodo(final String modulo, final String metodo, final IEmsRequest request)  {  
	    try {  
	    	Class<?> Classe = Class.forName(modulo);
	    	Method m = Classe.getDeclaredMethod(metodo, IEmsRequest.class);   
	        m.setAccessible(true);  
		    Object result = m.invoke(null, request);          
	        return result;  
	    } catch (NoSuchMethodException e) {  
	        // Essa exceção ocorre se o getMethod() não encontrar o método que  
	        // você especificou 
	    	print_log("Método não encontrado: " + metodo + ".");
	    	return metodo_not_implemented;
	    } catch (IllegalAccessException e) {  
	        // Pode ocorrer se o método que você está invocando não for  
	        // acessível. Você pode forçar que um método (mesmo privado!) seja  
	        // acessível fazendo:  
	        // antes do seu invoke.
	    	print_log("O método "+ modulo + "." + metodo + " não está acessível.");
	    	return metodo_not_visible;
	    } catch (InvocationTargetException e) {  
	        // Essa exceção acontece se o método chamado gerar uma exceção.  
	        // Use e.getCause() para descobrir qual exceção foi gerada no método  
	        // chamado e trata-la adequadamente.
	    	print_log("O método "+ modulo + "." + metodo + " gerou a exception: " + e.getCause() + ".");
	    	return negocio_exception;
	    } catch (ClassNotFoundException e) {
			print_log("EmsAgent: Módulo não encontrado: " + modulo + ".");
			return service_not_implemented;
		}
	}  	
	
	public void print_log(final String message){
		System.out.println(nomeAgente + ": " + message);
	}

	private class Task extends Thread{
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
            
            if (ret.getClass().getName().equals(Integer.class.getName())){
            	reply[1] = new OtpErlangInt((Integer) ret);
            }else if (ret.getClass().getName().equals(String.class.getName())){
            	reply[1] = new OtpErlangString((String) ret);
            }else if (ret instanceof Object){
            	reply[1] = new OtpErlangString((String) toJson(ret));
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
            
            otp_result[0] = servico;
            otp_result[1] = new OtpErlangLong(request.getRID());
            otp_result[2] = new OtpErlangTuple(reply);
            OtpErlangTuple myTuple = new OtpErlangTuple(otp_result);
            
            myMbox.send(from, myTuple);
        }  
	}
	
}
