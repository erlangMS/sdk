/*
 * 
 */
package br.erlangms.rest.discover;

/**
 *
 * @author Jader
 */
public class RestApiResourceDiscover {
    
    private Class<?> restAbstractFacadeClass;
    
    public RestApiResourceDiscover(final Class<?> restAbstractFacadeClass){
        this.restAbstractFacadeClass = restAbstractFacadeClass;
    }
    
    public RestApiResourceDiscover(final String restAbstractFacadeClassName) throws ClassNotFoundException {
        this.restAbstractFacadeClass = Class.forName(restAbstractFacadeClassName);
    }
    
}
