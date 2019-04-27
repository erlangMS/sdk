package br.erlangms.rest.cache;

import br.erlangms.rest.request.IRestApiRequest;

public interface IRestApiCacheEntry {
	public boolean isExpired();
	public IRestApiRequest getRequest();
	public Object getData();
	public void setData(Object data);
	public boolean isEmpty();
	public void clear();
	public int getHitCount();
	public void hit();
      	public int getWatermark();
	public void incrementWatermark();
}
