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
import java.util.Random;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

public final class EmsConnection implements Runnable {

    private static final Logger logger = EmsUtil.logger;
    private static final int THREAD_WAIT_TO_RESTART = 5000;
    private static final int CORE_POOL_SIZE = 12;
    private static final int MAX_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME_SECONDS = 60L;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_INTERNAL_ERROR = 500;
    private static final String ERRO_INTERNO_MSG = "Erro interno, contacte o administrador.";
    private static final ExecutorService pool = new java.util.concurrent.ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME_SECONDS, java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "ems-worker-" + count.getAndIncrement());
                }
            },
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

    private final String nameService;
    private final Object service;
    private final Class<? extends Object> classOfservice;
    private Method methods[];
    private String method_names[];
    private int method_count = 0;
    private final String otpNodeName;
    private OtpNode myNode = null;
    private OtpMbox myMbox = null;

    public EmsConnection(final Object service, final String otpNodeName, final boolean isSlave) {
        this.service = service;
        this.classOfservice = service.getClass();
        this.nameService = this.classOfservice.getName();
        final String host = "127.0.0.1";
        this.otpNodeName = otpNodeName.replace(".", "_") + "@" + host;
        getMethodNamesTable();
    }

    private void getMethodNamesTable() {
        Method[] allMethods = classOfservice.getMethods();
        methods = new Method[allMethods.length];
        method_names = new String[allMethods.length];
        for (Method m : allMethods) {
            if (m.getParameterCount() == 1) {
                Class<?> params[] = m.getParameterTypes();
                if (params[0] == IEmsRequest.class) {
                    methods[method_count] = m;
                    method_names[method_count] = m.getName();
                    m.setAccessible(true);
                    ++method_count;
                }
            }
        }
    }

    public void close() {
        try {
            if (myMbox != null) {
                myMbox.close();
            }
            if (myNode != null) {
                myNode.close();
            }
        } catch (Exception e) {
            logger.warning("Failed to close myMbox");
        }
    }

    public synchronized void createNode() throws InterruptedException {
        while (true) {
            Random r = new Random();
            try {
                myNode = new OtpNode(otpNodeName);
                String cookie = "erlangms";
                myNode.setCookie(cookie);
                // logger.info("✅ OtpNode created successfully: " + otpNodeName + ". Cookie: " +
                // cookie);
                return;
            } catch (Exception e) {
                if (Thread.interrupted())
                    throw new InterruptedException();
                logger.warning("❌ Failed to create OtpNode (" + otpNodeName + "): " + e.getMessage());
                try {
                    Thread.sleep(3000 + r.nextInt(3000));
                } catch (InterruptedException e1) {
                    if (Thread.interrupted())
                        throw e1;
                }
            }
        }
    }

    public synchronized void sendResult(final OtpErlangPid from, final OtpErlangTuple response) {
        myMbox.send(from, response);
    }

    @Override
    public void run() {
        OtpErlangObject myObject;
        OtpErlangTuple myMsg;
        OtpErlangTuple otp_request;
        OtpErlangPid dispatcherPid;
        EmsRequest request;
        final String msgReiniciarException = "Serviço " + nameService + " será reiniciado devido erro interno: ";

        while (true) {
            try {
                createNode();
                myMbox = myNode.createMbox(nameService);
                while (true) {
                    try {
                        myObject = myMbox.receive();
                        if (myObject instanceof OtpErlangTuple) {
                            logger.info("Message Received!");
                            myMsg = (OtpErlangTuple) myObject;
                            if (myMsg.arity() >= 2) {
                                otp_request = (OtpErlangTuple) myMsg.elementAt(0);
                                dispatcherPid = (OtpErlangPid) myMsg.elementAt(1);
                                request = new EmsRequest(otp_request);
                                pool.submit(new Task(dispatcherPid, request, this));
                            } else {
                                logger.warning("Received tuple with unexpected arity: " + myMsg.toString());
                            }
                        } else {
                            logger.warning("Received unknown object type: " + myObject.getClass().getName());
                        }

                    } catch (Exception e) {
                        logger.log(Level.INFO, msgReiniciarException + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (Exception e) {
                logger.info(msgReiniciarException + e.getMessage());
            } finally {
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
        new Thread(this, nameService).start();
    }

    private Object chamaMetodo(String modulo, String metodo, IEmsRequest request) {
        Method m = null;
        for (int i = 0; i < method_count; i++) {
            if (method_names[i].equals(metodo)) {
                m = methods[i];
                break;
            }
        }

        String msg_json;
        String className = service.getClass().getSimpleName();
        if (m != null) {
            try {
                logger.info("Invoking method: " + className + "." + metodo);
                Object ret = m.invoke(service, request);
                logger.info("Success: " + className + "." + metodo);
                return ret;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                while (cause instanceof InvocationTargetException && cause.getCause() != null) {
                    cause = cause.getCause();
                }
                while (cause instanceof Exception && cause.getCause() != null) {
                    if (cause instanceof EmsValidationException
                            || cause instanceof EmsNotFoundException
                            || cause instanceof IllegalArgumentException) {
                        break;
                    }
                    String causeName = cause.getClass().getName();
                    if (causeName.endsWith("EmsValidationException")
                            || causeName.endsWith("EmsNotFoundException")
                            || causeName.endsWith("ParseException")
                            || causeName.endsWith("JsonSyntaxException")) {
                        break;
                    }
                    cause = cause.getCause();
                }

                String causeName = cause.getClass().getName();
                boolean isNotFoundError = cause instanceof EmsNotFoundException
                        || causeName.endsWith("EmsNotFoundException");

                boolean isValidationOrParserError = cause instanceof EmsValidationException
                        || cause instanceof IllegalArgumentException
                        || causeName.endsWith("EmsValidationException")
                        || causeName.endsWith("ParseException")
                        || causeName.endsWith("JsonSyntaxException");

                int statusCode = HTTP_INTERNAL_ERROR;
                String errorType = "internal_error";
                String message;

                if (isValidationOrParserError || isNotFoundError) {
                    if (cause instanceof NumberFormatException) {
                        String causeMsg = cause.getMessage() != null ? cause.getMessage() : "";
                        causeMsg = causeMsg.replace("For input string: ", "").trim();
                        message = "Número ou formato numérico inválido: " + causeMsg;
                    } else if (cause instanceof IllegalArgumentException) {
                        message = "Argumento inválido: " + (cause.getMessage() != null ? cause.getMessage() : "");
                    } else if (causeName.endsWith("ParseException")) {
                        message = "Erro de formatação/conversão: "
                                + (cause.getMessage() != null ? cause.getMessage() : "");
                    } else if (causeName.endsWith("JsonSyntaxException")) {
                        message = "Sintaxe JSON inválida: " + (cause.getMessage() != null ? cause.getMessage() : "");
                    } else {
                        message = cause.getMessage() != null ? cause.getMessage() : cause.toString();
                    }
                    message = message.trim();
                    message = message.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");

                    if (isNotFoundError) {
                        statusCode = HTTP_NOT_FOUND;
                        errorType = "not_found";
                    } else {
                        statusCode = HTTP_BAD_REQUEST;
                        errorType = "validation";
                    }
                } else {
                    message = ERRO_INTERNO_MSG;
                }
                String erro = "Erro de validação ao invocar metodo " + className + "." + metodo + ": " + e.toString();
                logger.log(Level.SEVERE, erro, e);
                msg_json = "{\"error\":\"" + errorType + "\", \"message\" : \"" + message + "\"}";
                return new EmsResponse(statusCode, msg_json);
            } catch (Exception e) {
                String erro = "Erro ao invocar metodo " + className + "." + metodo + ": " + e.toString();
                logger.log(Level.SEVERE, erro, e);
                final String msgInternalError = "{\"error\":\"internal_error\", \"message\" : \"" + ERRO_INTERNO_MSG
                        + "\"}";
                return new EmsResponse(HTTP_INTERNAL_ERROR, msgInternalError);
            }
        } else {
            String erro = "Método não encontrado: " + className + "." + metodo;
            logger.warning(erro);
            final String msgNotFound = "{\"error\":\"method_not_found\", \"message\" : \"Falha ao chamar webservice\"}";
            return new EmsResponse(HTTP_NOT_FOUND, msgNotFound);
        }
    }

    private final class Task implements Callable<Boolean> {
        private final OtpErlangPid from;
        private final IEmsRequest request;
        private final EmsConnection connection;

        public Task(final OtpErlangPid from, final IEmsRequest request, EmsConnection connection) {
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
                    logger.info("Task completed, sending result");
                    connection.sendResult(from, response);
                }
            } catch (Exception e) {
                logger.severe("Task execution failed: " + e.getMessage());
            }
            return true;
        }
    }
}
