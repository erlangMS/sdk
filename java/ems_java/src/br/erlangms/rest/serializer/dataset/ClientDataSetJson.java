/*
 * 
 */
package br.erlangms.rest.serializer.dataset;

import java.io.Serializable;

/**
 *
 * @author Jader Adiel Schmitt
 */
public class ClientDataSetJson implements Serializable {
    private String data;
    public ClientDataSetJson(final String xmlDataSet){
        this.data = xmlDataSet;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
}
