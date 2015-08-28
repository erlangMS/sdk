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
public class CalculadoraService extends EmsServiceFacade {

	public CalculadoraService() throws Exception {
		super();
	}

	static public Integer soma(EmsRequest request) {
		Integer valor1 = Integer.parseInt(request.getQuery("valor1"));
		Integer valor2 = Integer.parseInt(request.getQuery("valor2"));
		return valor1 + valor2;
	}

}
