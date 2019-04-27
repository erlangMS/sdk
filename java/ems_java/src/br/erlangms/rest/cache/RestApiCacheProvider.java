package br.erlangms.rest.cache;

import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.request.IRestApiRequest;

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
    private int circular_index;  // este Ã­ndice anda pelo buffer de forma circular, ao chegar ao final, comemeÃ§a no inÃ­cio
    private int watermark;       // cada vez que o circular_index recomeÃ§a, incremental watermark
    private final IRestApiProvider apiProvider;
    private IRestApiCacheEntry lastEntry = null;

    public RestApiCacheProvider(final IRestApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        this.buffer = new RestApiCacheEntry[this.apiProvider.getContract().getCachePolicyConfig().getCapacity()];
        this.circular_index = 0;
        this.watermark = 0;
    }

    public synchronized IRestApiCacheEntry get(final IRestApiRequest request) {
        Integer key = request.hashCode();

        // Verifica se não foi a Ãºltima requisição gerada
        if (lastEntry != null && lastEntry.getRequest().hashCode() == key) {
            if (!lastEntry.isExpired()) {
                lastEntry.hit();
                lastEntry.getRequest().getStatistics().incrementUsedCount();
            } else {
                lastEntry.clear();
            }
            return lastEntry;
        }

        IRestApiCacheEntry result = null;

        // Varre os slots do buffer e localiza a requisição
        for (int i = 0; i < buffer.length; i++) {
            IRestApiCacheEntry entry = buffer[i];

            // Vamos parar no primeiro slot null ou quando um slot não tem request
            if (entry == null) {
                break;
            }

            if (entry.getRequest().hashCode() == key) {
                result = buffer[i];
                result.hit();
                if (!entry.isExpired()) {
                    result.getRequest().getStatistics().incrementUsedCount();
                } else {
                    result.clear();
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
                    watermark++;
                    if (watermark == 999999999) {
                        watermark = 0;
                    }
                }
            } while (result == null);
        }

        lastEntry = result;
        return result;
    }
}
