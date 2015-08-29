package br.erlangms.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.IEmsRequest;

@Singleton
@Startup
public class ContadorService extends EmsServiceFacade {
	private static int contador = 0;

	public ContadorService() throws Exception {
		super();
	}

	static public Integer count(IEmsRequest request) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return contador++;
	}

	static public Integer incrementBy(IEmsRequest request) {
		int step = Integer.parseInt(request.getParam("id"));
		contador = contador + step;
		return contador;
	}

}
