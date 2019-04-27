/*
 * 
 */
package br.erlangms.rest.discover;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import br.erlangms.EmsService;





/**
 * Classe responsável por descobrir e catalogar os web services em execução
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 02/04/2019
 *
 */
public class RestApiDiscover {

    private List<Object> listServices = new ArrayList<>();
    protected static final Logger logger = Logger.getLogger(RestApiDiscover.class.getName());
    private volatile boolean running = false;

    public void doScanServicesSpringboot(final String jarName, String packageName) {
        packageName = "BOOT-INF/classes/" + packageName.replaceAll("\\.", "/");
        try {
            try (JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName))) {
                JarEntry jarEntry;
                while (true) {
                    jarEntry = jarFile.getNextJarEntry();
                    if (jarEntry == null) {
                        break;
                    }
                    try {
                        if ((jarEntry.getName().startsWith(packageName)) && (jarEntry.getName().endsWith(".class"))) {
                            String classNamePath = jarEntry.getName()
                                    .replaceAll("/", "\\.")
                                    .replaceAll("^BOOT-INF.classes.", "")
                                    .replaceAll(".class$", "");
                            Class<?> serviceClass;
                            serviceClass = Class.forName(classNamePath);
                            if (serviceClass.isAnnotationPresent(EmsService.class)) {
                                //Object service = serviceClass.newInstance();
                                //startService(service);
                            }
                        }
                    } catch (Exception ex) {
                        logger.info("Ocorreu um erro ao iniciar " + jarEntry.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScanServices(final String packageName) {
        try {
            ClassLoader classLoader = RestApiDiscover.class.getClassLoader();
            String packagePath = packageName.replace('.', '/');
            URL url = classLoader.getResource(packagePath);
            VirtualFile file = VFS.getChild(url);
            List<VirtualFile> children = file.getChildrenRecursively();
            List<File> classes = new ArrayList<>();

            for (VirtualFile virtualFile : children) {
                if (virtualFile.isFile()) {
                    URL urlFile = virtualFile.asFileURL();
                    classes.add(new File(urlFile.getPath()));
                }
            }

            if (classes != null && classes.size() > 0) {
                for (File classe : classes) {
                    try {
                        if (classe.isFile()) {
                            String pathClassSlash = classe.getPath().split("/classes/")[1];
                            String classWithoutClass = pathClassSlash.split(".class")[0];
                            String classNamePath = classWithoutClass.replaceAll("/", ".");
                            Class<?> serviceClass;
                            serviceClass = Class.forName(classNamePath);
                            if (serviceClass.isAnnotationPresent(EmsService.class)) {
                                listServices.add(serviceClass);
                                System.out.println("Classe de serviÃ§o facade: "+ serviceClass.getSimpleName());

                            }
                        } else {
                            doScanServices(packageName + "." + classe.getName());
                        }
                    } catch (Exception ex) {
                        logger.info("Ocorreu um erro ao iniciar " + classe.getName());
                    }
                }
            } else {
                // Exemplo: "jar:file:/home/agilar/desenvolvimento/workspace/unb_servicos/target/unb_servicos-1.0.0-SNAPSHOT-exec.jar!/BOOT-INF/classes!/br/unb";
                String urlJar = url.toString()
                        .replaceAll("^jar:file:", "")
                        .replaceAll("!.*$", "");
                doScanServicesSpringboot(urlJar, packagePath);
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void scanServices(final String packageName) {
        if (running) {
            return;
        }
        running = true;
        doScanServices(packageName);
    }
}
