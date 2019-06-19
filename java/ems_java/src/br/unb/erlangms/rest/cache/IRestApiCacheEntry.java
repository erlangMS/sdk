package br.unb.erlangms.rest.cache;

import br.unb.erlangms.rest.request.IRestApiRequestInternal;
import java.time.LocalDateTime;

public interface IRestApiCacheEntry {
    public boolean isExpired();
    public IRestApiRequestInternal getRequest();
    public Object getData();
    public void saveData(Object data);
    public boolean isEmpty();
    public void clear();
    public int getRequestCacheHit();
    public void incrementRequestCacheHit();
    public int getWatermark();
    public long getStartTime();
    public void setStartTime(long startTime);
    public long getStopTime();
    public void setStopTime(long stopTime);
    public long getElapsedTime();
    public int getResultCacheHit();
    public void incrementResultCacheHit();
    public Integer getEstimatedSize();
    public void setEstimatedSize(Integer estimatedSize);
    public LocalDateTime getExpireDate();
}
