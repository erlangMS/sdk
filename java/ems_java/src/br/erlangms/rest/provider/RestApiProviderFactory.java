/*
 * 
 */
package br.erlangms.rest.provider;

import br.erlangms.rest.exception.RestApiConstraintException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fábrica de classes de provedores IRestApiProvider.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 17/04/2019
 *
 */
public final class RestApiProviderFactory {
    private static final Map<Class, IRestApiProvider> instances = new ConcurrentHashMap<>();
    private static IRestApiProvider lastApiProvider = null;

    public static IRestApiProvider createInstance(Class<IRestApiProvider> apiProviderClass) {
        try {
            if (lastApiProvider != null && lastApiProvider.getClass() == apiProviderClass){
                return lastApiProvider;
            }
            IRestApiProvider apiProvider = instances.get(apiProviderClass);
            if (apiProvider == null) {
                apiProvider = apiProviderClass.newInstance();
                instances.put(apiProviderClass, apiProvider);
                lastApiProvider = apiProvider;
            }
            return apiProvider;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RestApiConstraintException(RestApiConstraintException.PROVIDER_FACTORY_FAILED);
        }
    }

}
