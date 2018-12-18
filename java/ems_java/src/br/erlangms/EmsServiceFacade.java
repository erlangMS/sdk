/*********************************************************************
 * @title Módulo EmsServiceFacade
 * @version 1.0.0
 * @doc Classe de fachada para serviços ErlangMS
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;


/**
 * Classe de fachada para os serviços ErlangMS
 */
@EmsService
@Singleton
public abstract class EmsServiceFacade {

	@PostConstruct
    public void initialize() {
        EmsServiceScan.startService(this);
    }
    
}
