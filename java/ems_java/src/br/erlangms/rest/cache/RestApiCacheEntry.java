package br.erlangms.rest.cache;

import br.erlangms.rest.request.IRestApiRequest;
import java.time.LocalDateTime;

/**
 * Classe que implementa uma entrada no cache para uma requisição.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 27/03/2019
 *
 */
public class RestApiCacheEntry implements IRestApiCacheEntry {
    private final IRestApiRequest request;
    private Object data;        // armazena o dado da requisição
    private int hit;            // toda vez que o cache Ã© reutilizado ocorre um hit (acerto)
    // O watermark Ã© um marcador para indicar em que geraÃ§Ã£o do buffer circular a entrada de cache foi criada.
    // Toda vez que o buffer circular dÃ¡ uma volta completa, o watermark do buffer Ã© incrementado fazendo
    // com que este marcador referente a uma entrada do cache acabe ficando velho.
    // Marcadores mais velhos que o watermark do buffer podem ser reciclados para novas requisições
    private int watermark;
    private LocalDateTime expireDate;   // quando a entrada no cache vai expirar

    public RestApiCacheEntry(IRestApiRequest request, int bufferWatermark) {
        this.request = request;
        this.data = null;
        this.hit = 0;
        this.watermark = bufferWatermark;
    }

    @Override
    public IRestApiRequest getRequest() {
        return request;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public synchronized void setData(final Object data) {
        this.data = data;
        this.expireDate = LocalDateTime.now()
                .plusSeconds(request.getApiProvider()
                        .getContract()
                        .getCachePolicyConfig()
                        .getExpireTimeSeconds());
    }

    @Override
    public boolean isEmpty() {
        return data == null;
    }

    @Override
    public synchronized void clear() {
        this.data = null;
        this.expireDate = null;
        this.hit = 0;
    }

    @Override
    public boolean isExpired() {
        return expireDate != null && LocalDateTime.now().isAfter(expireDate);
    }

    @Override
    public int getWatermark() {
        return watermark;
    }
    @Override
    public void incrementWatermark() {
        watermark++;
    }

    @Override
    public int getHitCount() {
        return hit;
    }

    @Override
    public void hit() {
        hit += 1;
    }

}
