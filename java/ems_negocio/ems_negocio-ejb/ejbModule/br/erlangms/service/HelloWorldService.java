package br.erlangms.service;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import br.erlangms.IEmsRequest;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@LocalBean
@Startup
public class HelloWorldService extends EmsServiceFacade {

	public HelloWorldService() throws Exception {
		super();
	}

	static public String helloWorld(IEmsRequest request) {
		return "Hello World !!!";
	}

}
