/*********************************************************************
 * @title Módulo EmsConnection
 * @version 1.0.0
 * @doc Classe para conectar com o barramento msbus. Cada conexão representa um 
 *      agente e pode receber e enviar mensagens.  
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

public class EmsConnection
{
	private static int maxThreadPool;
	private static String cookie;
	private static String msbusHost;
	private static String hostName;
	private static String nodeName;
	private final String nomeAgente;
	private final String nomeService;
	private static final OtpErlangBinary result_ok; 
	private static final Logger logger;
	private final IEmsServiceFacade facade;
    private static OtpErlangPid dispatcherPid;
	private static String nodeUser;
	private static String nodePassword;
    private static String authorizationHeaderName;
    private static String authorizationHeaderValue;
	private OtpNode myNode = null;
	private OtpMbox myMbox = null;
	static{
		logger = Logger.getLogger(EmsConnection.class);
		result_ok = new OtpErlangBinary("{\"ok\":\"ok\"}".getBytes());
    	getSystemProperties();
    }
    
	public EmsConnection(final String nomeAgente, final String nomeService, final IEmsServiceFacade facade){
		this.nomeAgente = nomeAgente;
		this.nomeService = nomeService;
		this.facade = facade;
	}
	
	/**
	 * Obtem as configurações necessárias para executar os agentes
	 * Exemplo: 
	 *    -Dcookie=erlangms
	 *    -Dems_node=node01
	 *    -Dems_msbus=http://localhost:2301
	 *    -Dems_cookie=erlangms
	 *    -Dems_max_thread_pool_by_agent=10
	 *    -Dems_user=everton 
	 *    -Dems_password=123456 
	 * @param from pid do agente
	 * @return OtpErlangTuple
	 * @author Everton de Vargas Agilar
	 */
	private static void getSystemProperties() {
		String tmp_thread_pool = System.getProperty("ems_thread_pool");
		if (tmp_thread_pool != null){
			try{
				maxThreadPool = Integer.parseInt(tmp_thread_pool);
			}catch (NumberFormatException e){
				maxThreadPool = 12;
			}
		}else{
			maxThreadPool = 128;
		}
		String tmp_cookie = System.getProperty("ems_cookie");
		if (tmp_cookie != null){
		   cookie = tmp_cookie;
	   }else{
		   cookie = "erlangms";
	   }
	   try {
		   hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Não foi possível obter o hostname da máquina onde está o node.");
		}
	   String tmp_nodeName = System.getProperty("ems_node");
	   if (tmp_nodeName != null){
		   nodeName = tmp_nodeName;
	   }else{
		   nodeName = "node01";
	   }
	   String tmp_msbusHost = System.getProperty("ems_msbus");
	   if (tmp_msbusHost != null){
		   msbusHost = tmp_msbusHost;
	   }else{
		   msbusHost = "http://localhost:2301";
	   }
	   String tmp_user = System.getProperty("ems_user");
	   if (tmp_user != null){
		   nodeUser = tmp_user;
	   }else{
		   nodeUser = "";
	   }
	   String tmp_password = System.getProperty("ems_password");
	   if (tmp_password != null){
		   nodePassword = tmp_password;
	   }else{
		   nodePassword = "";
	   }
	   
       String usernameAndPassword = nodeUser + ":" + nodePassword;
       authorizationHeaderName = "Authorization";
       authorizationHeaderValue = "Basic " + java.util.Base64.getEncoder()
    		   .encodeToString(usernameAndPassword.getBytes());
	   
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
	
	public static String getHostName(){
		return hostName;
	}
	
	public static String getMsbusHost(){
		return msbusHost;
	}
	
	public static String getNodeUser() {
		return nodeUser;
	}

	public static String getNodePassword() {
		return nodePassword;
	}

	public static String getAuthorizationHeaderName() {
		return authorizationHeaderName;
	}

	public static String getAuthorizationHeaderValue() {
		return authorizationHeaderValue;
	}

	public void start() throws Exception {
		String otpNodeName = null;
		if (nodeName != null && !nodeName.isEmpty()){
		   otpNodeName = nomeAgente + "_" + nodeName;
	   }else{
		   otpNodeName = nomeAgente;
	   }
	   myNode = new OtpNode(otpNodeName);
       myNode.setCookie(cookie);
       StringBuilder msg_node = new StringBuilder(nomeService)
										.append(" host -> ").append(myNode.host())
										.append(" node -> ").append(myNode.node())
										.append(" port -> ").append(myNode.port())
										.append(" cookie -> ").append(myNode.cookie());
       logger.info(msg_node.toString());
       myMbox = myNode.createMbox(nomeService);
       OtpErlangObject myObject;
       OtpErlangTuple myMsg;
       OtpErlangTuple otp_request;
       IEmsRequest request;
       StringBuilder msg_task = new StringBuilder();
       ExecutorService pool = Executors.newFixedThreadPool(maxThreadPool);
       while(true){ 
    	   try {
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
                pool.submit(new Task(dispatcherPid, request, myMbox));
			} catch(OtpErlangExit e) {
				break;
	        }
       }
    }
	
	public void close(){
		if (myNode != null){
			try{
				myNode.close();
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				logger.info(nomeService + " finalizado.");
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
	    	logger.info(erro);
	    	return "{\"erro\":\"service\", \"message\" : \"" + erro + "\"}";
	    } catch (IllegalAccessException e) {  
	        // Pode ocorrer se o método que você está invocando não for  
	        // acessível. Você pode forçar que um método (mesmo privado!) seja  
	        // acessível fazendo:  
	        // antes do seu invoke.
	    	String erro = "Acesso ilegal ao método de negócio: " + metodo + ".";
	    	logger.info(erro);
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
			    	logger.info(erro);
			    	return "{\"erro\":\"service\", \"message\" : \"" + erro + "\"}";
	    		}
	    	}else{
		    	String erro = "O método "+ modulo + "." + metodo + " gerou uma excessão: " + e.getCause() + "."; 
		    	logger.info(erro);
		    	return "{\"erro\":\"service\", \"message\" : " + EmsUtil.toJson(erro) + "}";
	    	}
	    }
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
