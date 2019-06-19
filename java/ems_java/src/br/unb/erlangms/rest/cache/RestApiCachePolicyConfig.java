package br.unb.erlangms.rest.cache;

import br.unb.erlangms.rest.exception.RestApiException;

/**
 * Classe de implementação para a interface IRestApiCachePolicyConfig.
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public class RestApiCachePolicyConfig implements IRestApiCachePolicyConfig {
    public final static int DEFAULT_EXPIRE_TIME_SECONDS = 60;
    public final static int DEFAULT_CAPACITY = 60;
    public final static int DEFAULT_ENTRY_SIZE_BYTES = 256000; // 256KB
    private int expireTimeSeconds = DEFAULT_EXPIRE_TIME_SECONDS;
    private boolean allowRequestCache = true;
    private boolean allowResultCache = true;
    private int capacity = DEFAULT_CAPACITY;
    private int entrySizeBytes = DEFAULT_ENTRY_SIZE_BYTES;

    @Override
    public int getExpireTimeSeconds() {
        return expireTimeSeconds;
    }

    @Override
    public void setExpireTimeSeconds(int expireTime) {
        this.expireTimeSeconds = expireTime;
    }

    @Override
    public boolean isAllowRequestCache() {
        return allowRequestCache;
    }

    @Override
    public void setAllowRequestCache(boolean allowRequestCache) {
        this.allowRequestCache = allowRequestCache;
    }

    @Override
    public boolean isAllowResultCache() {
        return allowResultCache;
    }

    @Override
    public void setAllowResultCache(boolean allowResultResult) {
        if (!allowRequestCache){
            throw new RestApiException(RestApiException.RESULT_CACHE_PERMITIDO_SE_REQUEST_CACHE_ATIVO);
        }
        this.allowResultCache = allowResultResult;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public int getEntrySizeBytes() {
        return entrySizeBytes;
    }

    @Override
    public void setEntrySizeBytes(int entrySizeKB) {
        this.entrySizeBytes = entrySizeKB;
    }
}
