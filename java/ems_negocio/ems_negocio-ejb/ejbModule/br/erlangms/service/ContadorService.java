package br.erlangms.service;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import br.erlangms.EmsRequest;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@LocalBean
@Startup
public class ContadorService extends EmsServiceFacade {

	public ContadorService() throws Exception {
		super();
	}

	static public Integer count(EmsRequest request) {
		return 10;
	}

}
