/*********************************************************************
 * @title Módulo EmsConnection
 * @version 1.0.0
 * @doc Classe de conexão com o barramento ErlangMS
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

public class EmsConnection implements Runnable {
    
    private static final Logger logger = EmsUtil.logger;
    private static final int THREAD_WAIT_TO_RESTART = 5000;
    private final String nameService;
    private final Object service;
    private Class<? extends Object> classOfservice;
    private Method methods[];
    private String method_names[];
    private int method_count = 0;
    private String otpNodeName;
    private OtpNode myNode = null;
    private OtpMbox myMbox = null;
    
    public EmsConnection(final Object service, final String otpNodeName, final boolean isSlave){
        this.service = service;
        this.classOfservice = service.getClass();
        this.nameService = this.classOfservice.getName();
        
        // Hardcode host to 127.0.0.1 to avoid "illegal hostname" errors in Erlang with long names
        String nodeSuffix = ""; 
        String host = "127.0.0.1";
        
        this.otpNodeName = otpNodeName.replace(".",  "_") + nodeSuffix + "@" + host;
        
        logger.info("Initializing EmsConnection. Target Node Name: " + this.otpNodeName);
        
        getMethodNamesTable();
    }
    

    private void getMethodNamesTable() {
        Method[] allMethods = classOfservice.getMethods(); 
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
        }
    }
    
    public synchronized void createNode() throws InterruptedException {
        while (true){
            Random r = new Random();
            try{
                // Constructor with single argument parses it as name@host
                myNode = new OtpNode(otpNodeName);
                
                String cookie = "erlangms";
                myNode.setCookie(cookie);
                logger.info("✅ OtpNode created successfully: " + otpNodeName + ". Cookie: " + cookie);
                return;
            }catch (Exception e){
                if (Thread.interrupted()) throw new InterruptedException();
                logger.warning("❌ Failed to create OtpNode (" + otpNodeName + "): " + e.getMessage());
                try{
                    Thread.sleep(3000 + r.nextInt(3000));
                }catch (InterruptedException e1){
                    if (Thread.interrupted()) throw e1;
                }
            }
        }
    }
    
    public synchronized void sendResult(final OtpErlangPid from, final OtpErlangTuple response) {
        logger.info("📤 Sending response to: " + from);
        myMbox.send(from, response);
    }
    
    @Override  
    public void run() {
        OtpErlangObject myObject;
        OtpErlangTuple myMsg;
        OtpErlangTuple otp_request;
        OtpErlangPid dispatcherPid;
        EmsRequest request;
        ExecutorService pool = Executors.newCachedThreadPool();
        final String msgReiniciarException = "Serviço "+ nameService + " será reiniciado devido erro interno: ";
        
        while (true){
            try {
                   createNode();
                   myMbox = myNode.createMbox(nameService);
                   logger.info("📬 Mailbox registered: " + nameService + " on node " + myNode.node());
                   
                   // Message Loop
                   while (true) {
                        try {  
                            logger.info("Aguardando mensagem do barramento...");
                            myObject = myMbox.receive();  
                            logger.info("📥 Message Received! " + myObject.toString());
                            
                            if (myObject instanceof OtpErlangTuple) {
                                myMsg = (OtpErlangTuple) myObject;
                                if (myMsg.arity() >= 2) {
                                    otp_request = (OtpErlangTuple) myMsg.elementAt(0);
                                    dispatcherPid = (OtpErlangPid) myMsg.elementAt(1);
                                    
                                    logger.info("⚙️ Processing request from PID: " + dispatcherPid);
                                    request = new EmsRequest(otp_request); 
                                    pool.submit(new Task(dispatcherPid, request, this));
                                } else {
                                    logger.warning("⚠️ Received tuple with unexpected arity: " + myMsg.toString());
                                }
                            } else {
                                logger.warning("⚠️ Received unknown object type: " + myObject.getClass().getName());
                            }
                            
                        } catch (Exception e) {  
                            logger.log(Level.INFO, msgReiniciarException + e.getMessage());
                            e.printStackTrace();
                            break;
                        }
                   }
            } catch (Exception e) {
                logger.info(msgReiniciarException + e.getMessage());
            }finally{
                close();
            }
            
            try {
                Thread.sleep(THREAD_WAIT_TO_RESTART);
            } catch (InterruptedException e) {
                break;
            }
        }
    }  

    public void start() {  
        new Thread(this).start();  
    }  
    
    private Object chamaMetodo(String modulo, String metodo, IEmsRequest request) {
        Method m = null;
        for (int i=0; i < method_count; i++){
            if (method_names[i].equals(metodo)){
                m = methods[i];
                break;
            }
        }
        
        String msg_json;
        if (m != null){
            String className = service.getClass().getSimpleName();
            try {
                logger.info("Invoking method: " + className + "." + metodo);
                Object ret = m.invoke(service, request);
                logger.info("Success: " + className + "." + metodo);
                return ret;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                String erro = "Erro na execucao do metodo " + className + "." + metodo + ": " + (cause != null ? cause.toString() : e.toString());
                logger.warning(erro);
                e.printStackTrace();
                msg_json = "{\"error\":\"internal_error\", \"message\" : \"" + erro.replace("\"", "\"") + "\"}" ; 
                return new EmsResponse(500, msg_json);
            } catch (Exception e) {
                String erro = "Erro ao invocar metodo " + className + "." + metodo + ": " + e.toString();
                logger.warning(erro);
                msg_json = "{\"error\":\"internal_error\", \"message\" : \"" + erro.replace("\"", "\"") + "\"}" ; 
                return new EmsResponse(500, msg_json);
            }
        }else{
            String erro = "Método não encontrado: " + metodo;
            msg_json = "{\"error\":\"method_not_found\", \"message\" : \"" + erro + "\"}" ; 
            logger.warning(erro);
            return new EmsResponse(404, msg_json);
        }
    }
    
    private final class Task implements Callable<Boolean>{
        private OtpErlangPid from;
        private IEmsRequest request;
        private EmsConnection connection;
        
        public Task(final OtpErlangPid from, final IEmsRequest request, EmsConnection connection){
            super();
            this.from = from;
            this.request = request;
            this.connection = connection;
        }
        
    public Boolean call() {  
        try {
            Object ret = chamaMetodo(request.getModulo(), request.getFunction(), request);
            if (request.getRID() > 0) {
                 OtpErlangTuple response = EmsUtil.serializeObjectToErlangResponse(ret, request);
                 logger.info("✅ Task completed. Sending result to " + from);
                 connection.sendResult(from, response); 
            }
        } catch (Exception e) {
            logger.severe("Task execution failed: " + e.getMessage());
        }
        return true;
    }  
    }
}
