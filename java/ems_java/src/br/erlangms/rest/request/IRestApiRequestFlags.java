/*
 * 
 */
package br.erlangms.rest.request;

import java.io.Serializable;

/**
 * Os flags permitem alterar o comportamento de uma requisição em tempo de execução.
 *
 * Foi criado para finalidades de depuração.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 25/04/2019
 *
 */
public interface IRestApiRequestFlags extends Serializable {
    boolean isLogQueryCreated();
    boolean isLogStatistics();
    boolean isNoCache();
    boolean isNoNamedQuery();
    boolean isNoRequestCache();
    boolean isNoResultCache();
    boolean isSlowTest();
    void setLogQueryCreated(boolean logQueryCreated);
    void setLogStatistics(boolean logStatistics);
    void setNoCache(boolean noCache);
    void setNoNamedQuery(boolean noNamedQuery);
    void setNoRequestCache(boolean noRequestCache);
    void setNoResultCache(boolean noResultCache);
    void setSlowTest(boolean slowTest);

    void setFlagsAsString(String flags);
}
