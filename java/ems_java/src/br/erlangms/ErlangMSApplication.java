package br.erlangms;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class ErlangMSApplication{
	
	private static List<EmsConnection> listServices = new ArrayList<EmsConnection>();
	private static final Logger logger = EmsUtil.logger;
	private static volatile boolean running = false; 
	
	
	public static void scanServiceInJar(String jarName, String packageName){
		  packageName = "BOOT-INF/classes/" + packageName.replaceAll("\\." , "/");
		  try{
			    try ( JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName))){
				    JarEntry jarEntry;
				    while(true) {
			    	  jarEntry=jarFile.getNextJarEntry();
			    	  if (jarEntry == null) break;
				      try {
				    	  if((jarEntry.getName().startsWith (packageName)) && (jarEntry.getName ().endsWith (".class")) ) {
						        String classNamePath = jarEntry.getName()
						        							.replaceAll("/", "\\.")
						        							.replaceAll("^BOOT-INF.classes.", "")
						        							.replaceAll(".class$", "");
						        Class<?> serviceClass;
						        serviceClass = Class.forName(classNamePath);
						        if (serviceClass.isAnnotationPresent(EmsService.class)) {
						        	Object service = serviceClass.newInstance();
					        		startService(service);
						        }
					      }
				        }catch (Exception ex) {
				        	logger.info("Ocorreu um erro ao iniciar "+ jarEntry.getName());
				        }
				    }
			    }
		  } catch( Exception e){
			  e.printStackTrace ();
		  }
	}
	
	@PostConstruct
	public static void run(){
		if (running) return;
		running = true;
		logger.info("Start services ErlangMS from "+ EmsUtil.properties.service_scan);
    	scanServices(EmsUtil.properties.service_scan);
	}
	
	private static void scanServices(final String packageName){
	    try {
		    ClassLoader classLoader = ErlangMSApplication.class.getClassLoader();
		    String packagePath  = packageName.replace('.', '/');
		    URL url = classLoader.getResource(packagePath);
		    File folder = new File(url.getPath());
		    File[] classes = folder.listFiles();
		    if (classes != null && classes.length > 0) {
			    for(File classe : classes){
			        try {
				        if (classe.isFile()) {
				        	int index = classe.getName().indexOf(".");
				        	String className = classe.getName().substring(0, index);
					        String classNamePath = packageName+ "." + className;
					        Class<?> serviceClass;
					        serviceClass = Class.forName(classNamePath);
					        if (serviceClass.isAnnotationPresent(EmsService.class)) {
					        	Object service = serviceClass.newInstance();
				        		startService(service);
					        }
				        }else {
				        	scanServices(packageName + "." + classe.getName());
				        }
			        }catch (Exception ex) {
			        	logger.info("Ocorreu um erro ao iniciar "+ classe.getName());
			        }
			    }
		    }else {
		    	// Exemplo: "jar:file:/home/agilar/desenvolvimento/workspace/unb_servicos/target/unb_servicos-1.0.0-SNAPSHOT-exec.jar!/BOOT-INF/classes!/br/unb";
				String urlJar = url.toString()
									.replaceAll("^jar:file:", "")
									.replaceAll("!.*$", "");
				scanServiceInJar(urlJar, packagePath);
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

	private void stopServices() {
		for (EmsConnection connection : listServices) {
			try {
				connection.close();
				connection.interrupt();
			}catch (Exception e) {
				// não faz nada
			}
		}
	}
	
	@PreDestroy
    public void terminate() {
		logger.info("Stopping services ErlangMS...");
		stopServices();
	}

	
}  