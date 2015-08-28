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
public class CalculadoraService extends EmsServiceFacade {

	public CalculadoraService() throws Exception {
		super();
	}

	static public Integer soma(IEmsRequest request) {
		Integer valor1 = Integer.parseInt(request.getQuery("valor1"));
		Integer valor2 = Integer.parseInt(request.getQuery("valor2"));
		return valor1 + valor2;
	}

	static public int subtrai(IEmsRequest request) {
		Integer valor1 = Integer.parseInt(request.getQuery("valor1"));
		Integer valor2 = Integer.parseInt(request.getQuery("valor2"));
		return valor1 - valor2;
	}

	static public int multiplica(IEmsRequest request) {
		Integer valor1 = Integer.parseInt(request.getQuery("valor1"));
		Integer valor2 = Integer.parseInt(request.getQuery("valor2"));
		return valor1 * valor2;
	}

	static public int divide(IEmsRequest request) {
		Integer valor1 = Integer.parseInt(request.getQuery("valor1"));
		Integer valor2 = Integer.parseInt(request.getQuery("valor2"));
		return valor1 / valor2;
	}
	
}
