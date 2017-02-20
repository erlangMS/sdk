package br.erlangms.samples.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.EmsUtil;
import br.erlangms.IEmsRequest;

@Singleton
@Startup
public class HelloWorldService extends EmsServiceFacade {

	public HelloWorldService() throws Exception {
		super();
	}

	public String helloWorld(IEmsRequest request) {
		
		String[] anexos = new String[1];
		anexos[0] = "/home/everton/desenvolvimento/erlangms/ems-bus/build.sh";
		EmsUtil.sendTextMail("evertonagilar@gmail.com", "isso é um teste", "contedo do email", anexos);
		return "{\"message\":\"Hello World Java!!!\"}";
	}
	

}
