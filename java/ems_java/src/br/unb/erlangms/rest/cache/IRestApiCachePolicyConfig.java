package br.unb.erlangms.rest.cache;

/**
 * Interface que cont�m configura��es para a pol�tica do subsistema de cache da API REST.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public interface IRestApiCachePolicyConfig {
    int getCapacity();
    int getEntrySizeBytes();
    int getExpireTimeSeconds();
    boolean isAllowRequestCache();
    boolean isAllowResultCache();
    void setAllowRequestCache(boolean allowRequestCache);
    void setAllowResultCache(boolean allowResultResult);
    void setCapacity(int capacity);
    void setEntrySizeBytes(int entrySizeKB);
    void setExpireTimeSeconds(int expireTime);

}
