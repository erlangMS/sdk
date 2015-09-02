package br.erlangms.samples;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsRequest;
import br.erlangms.IEmsServiceFacade;

@Singleton
@Startup
public class HelloWorldService extends EmsServiceFacade implements IEmsServiceFacade {

	public HelloWorldService() throws Exception {
		super();
	}

	public String helloWorld(IEmsRequest request) {
		return "Hello World Java!!!";
	}

}
