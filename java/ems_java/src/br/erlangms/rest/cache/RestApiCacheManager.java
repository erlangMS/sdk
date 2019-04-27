package br.erlangms.rest.cache;

import br.erlangms.rest.request.IRestApiRequest;
import java.util.Map;

public final class RestApiCacheManager {
	private static final Map<Class, RestApiCacheProvider> entries = new java.util.concurrent.ConcurrentHashMap<>();

	public static IRestApiCacheEntry get(final IRestApiRequest request) {
		Class key = request.getApiProvider().getClass();
		RestApiCacheProvider entry = entries.get(key);
		if (entry == null) {
			entry = new RestApiCacheProvider(request.getApiProvider());
			entries.put(key, entry);
		}
		return entry.get(request);
	}

}