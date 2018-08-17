/*********************************************************************
 * @title Módulo EmsServiceProxy
 * @version 1.0.0
 * @doc Classe proxy para serviços ErlangMS remotos
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

public abstract class EmsServiceProxy {

	public EmsServiceStream getStream(){
		return new EmsServiceStream();
	}
	
}
