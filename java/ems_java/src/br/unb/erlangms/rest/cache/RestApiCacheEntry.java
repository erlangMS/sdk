package br.unb.erlangms.rest.cache;

import br.unb.erlangms.rest.request.IRestApiRequestInternal;
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
    private final IRestApiRequestInternal request;
    private Object data;                                    // armazena o dado da requisição
    private int requestCacheHit;            // toda vez que o cache de requisição é reutilizado ocorre um incrementRequestCacheHit (acerto)
    // O watermark é um marcador para indicar em que geração do buffer circular a entrada de cache foi criada.
    // Toda vez que o buffer circular dá uma volta completa, o watermark do buffer é incrementado fazendo
    // com que este marcador referente a uma entrada do cache acabe ficando velho.
    // Marcadores mais velhos que o watermark do buffer podem ser reciclados para novas requisições
    private final int watermark;
    private LocalDateTime expireDate;   // quando a entrada no cache vai expirar
    private long startTime;
    private long stopTime;
    private long elapsedTime;
    private int resultCacheHit;
    private Integer estimatedSize;

    public RestApiCacheEntry(final IRestApiRequestInternal request, int bufferWatermark) {
        this.request = request;
        this.requestCacheHit = 0;
        this.watermark = bufferWatermark;  // começa igual ao do buffer mas vai ser incrementado
        this.startTime = 0L;
        this.stopTime = 0L;
        this.elapsedTime = 0L;
        this.resultCacheHit = 0;
        this.estimatedSize = null;
    }

    @Override
    public IRestApiRequestInternal getRequest() {
        return request;
    }

    @Override
    public synchronized void saveData(final Object data) {
        this.data = data;
        this.expireDate = LocalDateTime.now()
                .plusSeconds(request.getApiProvider()
                        .getContract()
                        .getCachePolicyConfig()
                        .getExpireTimeSeconds());
    }

    @Override
    public boolean isEmpty() {
        return this.data == null;
    }

    @Override
    public synchronized void clear() {
        this.data = null;
        this.startTime = 0L;
        this.stopTime = 0L;
        this.elapsedTime = 0L;
        this.resultCacheHit = 0;
        this.estimatedSize = null;
        this.expireDate = null;
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
    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    @Override
    public int getRequestCacheHit() {
        return requestCacheHit;
    }

    @Override
    public void incrementRequestCacheHit() {
        requestCacheHit += 1;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getStopTime() {
        return stopTime;
    }

    @Override
    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
        this.elapsedTime = this.stopTime - this.startTime;
    }

    @Override
    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public Integer getEstimatedSize() {
        return estimatedSize;
    }

    @Override
    public void setEstimatedSize(Integer estimatedSize) {
        this.estimatedSize = estimatedSize;
    }

    @Override
    public int getResultCacheHit() {
        return resultCacheHit;
    }

    @Override
    public void incrementResultCacheHit() {
        this.resultCacheHit += 1;
    }


}
