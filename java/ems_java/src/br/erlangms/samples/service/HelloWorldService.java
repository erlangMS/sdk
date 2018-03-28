package br.erlangms.samples.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;

@Singleton
@Startup
public class HelloWorldService extends EmsServiceFacade {

	public HelloWorldService() throws Exception {
		super();
	}

	public String helloWorld(IEmsRequest request) {
		return "{\"message\":\"Hello World Java!!!\"}";
	}
	

}
