package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.schema.RestJoinType;
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
    boolean isLogQuery();
    boolean isLogRequest();
    boolean isNoNamedQuery();
    boolean isNoRequestCache();
    boolean isNoResultCache();
    boolean isNoCache();
    boolean isSlowTest();
    boolean isAllFields();
    boolean isNoPaginate();
    void setLogQuery(boolean logQueryCreated);
    void setLogRequest(boolean logStatistics);
    void setNoNamedQuery(boolean noNamedQuery);
    void setNoRequestCache(boolean noRequestCache);
    void setNoResultCache(boolean noResultCache);
    void setNoCache(boolean noCache);
    void setSlowTest(boolean slowTest);
    void setAllFields(boolean allFields);
    void setNoPaginate(boolean noLimit);
    void setFlagsAsString(String flags);
    void setJoinType(RestJoinType fetchJoin);
    public RestJoinType getJoinType();
}
