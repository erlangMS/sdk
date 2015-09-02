package br.erlangms.samples;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.erlangms.EmsServiceFacade;
import br.erlangms.IEmsServiceFacade;

@Singleton
@Startup
public class ContadorService extends EmsServiceFacade implements IEmsServiceFacade {
	private int i = 0;
	
	public ContadorService() throws Exception {
		super();
	}

	public int contador() {
		return i++;
	}

}
