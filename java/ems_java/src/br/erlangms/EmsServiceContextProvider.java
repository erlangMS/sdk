package br.erlangms;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class EmsServiceContextProvider{
	
	private static List<EmsConnection> listServices = new ArrayList<EmsConnection>();
	private static final Logger logger = EmsUtil.logger;
	
	public void startServices(String packageName){
	    try {
		    //Get the package name from configuration file
		    //String packageName = "br.unb.pessoal.facade";
	
		    //Load the classLoader which loads this class.
		    ClassLoader classLoader = getClass().getClassLoader();
	
		    //Change the package structure to directory structure
		    String packagePath  = packageName.replace('.', '/');
		    URL urls = classLoader.getResource(packagePath);
	
		    //Get all the class files in the specified URL Path.
		    File folder = new File(urls.getPath());
		    File[] classes = folder.listFiles();
	
		    for(File classe : classes){
		        try {
			    	int index = classe.getName().indexOf(".");
			        if (classe.isFile()) {
				    	String className = classe.getName().substring(0, index);
				        String classNamePath = packageName+ "." + className;
				        //System.out.println(classNamePath);
				        Class<?> repoClass;
				        repoClass = Class.forName(classNamePath);
				        //Annotation[] annotations = repoClass.getAnnotations();
				        if (repoClass.isAnnotationPresent(EmsService.class)) {
				        	if (!repoClass.isAnnotationPresent(Startup.class)){
				        		Object service = repoClass.newInstance();
				        		startService(service);
				        	}
				        }
				        
				        /*for(int j =0;j<annotations.length;j++){
				            System.out.println("Annotation in class "+repoClass.getName()+ " is "+annotations[j].annotationType().getName());
				        }*/
			        }else {
			        	//System.out.println("Pacote " + classe.getName());
			        	startServices(packageName+ "." + classe.getName());
			        }
			        
		        }catch (Exception ex) {
		        	logger.info("Ocorreu um erro ao iniciar "+ classe.getName());
		        }
		    }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void startService(final Object service) {
		String classNameOfService = service.getClass().getName();
		//logger.info("EmsServiceScan start "+ classNameOfService);
		EmsConnection connection1 = new EmsConnection(service, classNameOfService, false);
		connection1.start();
		listServices.add(connection1);
		if (EmsUtil.properties.isLinux) {
			EmsConnection connection2 = new EmsConnection(service, classNameOfService + "02", true);
			connection2.start();
			listServices.add(connection2);
		}
	}

	public void stopServices() {
		for (EmsConnection connection : listServices) {
			try {
				connection.close();
				connection.interrupt();
			}catch (Exception e) {
				// não faz nada
			}
		}
	}
	
    @PostConstruct
    public void initialize() {
    	logger.info("Initialize EmsServiceContextProvider...");
    	startServices(EmsUtil.properties.service_scan);
    }

	@PreDestroy
    public void terminate() {
		logger.info("Finalize EmsServiceContextProvider...");
		stopServices();
	}

	
}  