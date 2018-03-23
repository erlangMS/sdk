package br.erlangms.samples.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.EmsServiceStream;
import br.erlangms.IEmsRequest;

@Singleton
@Startup
public class HelloWorldService extends EmsServiceFacade {

	public HelloWorldService() throws Exception {
		super();
	}

	public String helloWorld(IEmsRequest request) {

		

			
			EmsServiceStream rest = new EmsServiceStream();			
			Object usuario = rest.from("/auth/user/:id?fields=email")
				.setParameter(27879)
				.request()
				.getObject();
			
			System.out.println(usuario);	
		
		return "{\"message\":\"Hello World Java!!!\"}";
	}
	

}
