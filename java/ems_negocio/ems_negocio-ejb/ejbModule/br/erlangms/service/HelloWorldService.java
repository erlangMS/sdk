package br.erlangms.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.IEmsRequest;

@Singleton
@Startup
public class HelloWorldService extends EmsServiceFacade {

	public HelloWorldService() throws Exception {
		super();
	}

	static public String helloWorld(IEmsRequest request) {
		return "Hello World !!!";
	}

}
