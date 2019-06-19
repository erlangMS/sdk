/*********************************************************************
 * @title Módulo EmsServiceFacade
 * @version 1.0.0
 * @doc Classe de fachada para serviços ErlangMS
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.unb.erlangms;

import javax.annotation.PostConstruct;


/**
 * Classe de fachada para os serviços ErlangMS
 */
public abstract class EmsServiceFacade {

    @PostConstruct
    public void initialize() {
        ErlangMSApplication.startService(this);
    }

}
