package br.unb.erlangms.rest.cache;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.request.IRestApiRequestFlags;
import br.unb.erlangms.rest.request.IRestApiRequestInternal;

/**
 * Classe que implementa o cache para um RestApiProvider
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 27/03/2019
 *
 */
public final class RestApiCacheProvider {
    private final IRestApiCacheEntry buffer[];  // buffer onde os slots das requisições são armazenadas
    private int circular_index;  // este índice anda pelo buffer de forma circular, ao chegar ao final, comemeça no início
    private int watermark;       // cada vez que o circular_index recomeça, incrementa watermark
    private final IRestApiProvider apiProvider;
    private IRestApiCacheEntry lastEntry = null;

    public RestApiCacheProvider(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        this.buffer = new RestApiCacheEntry[this.apiProvider.getContract().getCachePolicyConfig().getCapacity()];
        this.circular_index = 0;
        this.watermark = 0;
    }

    public synchronized IRestApiCacheEntry get(final IRestApiRequestInternal request) {
        int RID = request.getRID();
        IRestApiRequestFlags flags = request.getRequestUser().getFlags();
        boolean isNoAnyCache = flags.isNoCache() || flags.isNoResultCache() || flags.isNoRequestCache();

        if (isNoAnyCache){
            return new RestApiCacheEntry(request, watermark);
        }

        // Verifica se não foi a última requisição gerada
        if (lastEntry != null && lastEntry.getRequest().getRID() == RID) {
            lastEntry.incrementRequestCacheHit();
            if (lastEntry.isExpired() || isNoAnyCache) {
                lastEntry.clear();
            } else {
                lastEntry.incrementResultCacheHit();
            }
            return lastEntry;
        }

        IRestApiCacheEntry result = null;

        // Varre os slots do buffer e localiza a requisição
        for (IRestApiCacheEntry entry : buffer) {
            // Vamos parar no primeiro slot null ou quando um slot não tem request
            if (entry == null) {
                break;
            }

            // A requisição tem que ter o mesmo RID (Request ID) e o watermark igual ou superior)
            if (entry.getRequest().getRID() == RID && entry.getWatermark() >= getWatermark()) {
                result = entry;
                result.incrementRequestCacheHit();
                if (entry.isExpired() || isNoAnyCache) {
                    result.clear();
                } else {
                    result.incrementResultCacheHit();
                }
                break;
            }
        }

        // Como não achou o request, vai precisar alocar um slot
        if (result == null) {
            do {
                IRestApiCacheEntry currentEntry = buffer[circular_index];
                if (currentEntry == null || currentEntry.isExpired() || currentEntry.getWatermark() <= watermark) {
                    result = new RestApiCacheEntry(request, watermark);
                    buffer[circular_index] = result;
                }
                circular_index++;
                if (circular_index == buffer.length) {
                    circular_index = 0;
                    result = new RestApiCacheEntry(request, watermark);
                    buffer[circular_index] = result;
                    if (watermark == 999999999) {
                        watermark = 0;
                    }
                }
            } while (result == null);
        }

        lastEntry = result;
        return result;
    }

    public int getCircularIndex() {
        return this.circular_index;
    }

    public int getWatermark() {
        return this.watermark;
    }


    public synchronized void clear() {
        this.lastEntry = null;
        this.watermark = this.watermark + 1;
    }
}
