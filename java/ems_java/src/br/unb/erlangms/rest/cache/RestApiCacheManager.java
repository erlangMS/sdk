package br.unb.erlangms.rest.cache;

import br.unb.erlangms.rest.provider.IRestApiProvider;
import java.util.Map;

public final class RestApiCacheManager {
	private static final Map<Class, RestApiCacheProvider> entries = new java.util.concurrent.ConcurrentHashMap<>();

	public static RestApiCacheProvider get(final IRestApiProvider apiProvider) {
		Class key = apiProvider.getClass();
		RestApiCacheProvider cacheProvider = entries.get(key);
		if (cacheProvider == null) {
			cacheProvider = new RestApiCacheProvider(apiProvider);
			entries.put(key, cacheProvider);
		}
        return cacheProvider;
	}

}
