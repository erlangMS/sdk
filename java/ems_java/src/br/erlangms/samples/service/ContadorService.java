package br.erlangms.samples.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;

@Singleton
@Startup
public class ContadorService extends EmsServiceFacade {
	private int i = 0;
	
	public ContadorService() throws Exception {
		super();
	}

	public int contador() {
		return i++;
	}

}
