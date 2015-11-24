/*********************************************************************
 * @title Módulo EmsServiceProxy
 * @version 1.0.0
 * @doc Classe proxy para serviços ErlangMS remotos
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms.service;

import br.erlangms.EmsServiceFacade;
import br.erlangms.EmsServiceStream;

public abstract class EmsServiceProxy extends EmsServiceFacade {

	public EmsServiceStream getStream(){
		return new EmsServiceStream();
	}
	

}
