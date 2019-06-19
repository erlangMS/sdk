/*********************************************************************
 * @title Interface IEmsRequest
 * @version 1.0.0
 * @doc Interface que representa uma requisição para um serviço
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 ********************/
  
package br.unb.erlangms;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ericsson.otp.erlang.OtpErlangObject;

public interface IEmsRequest {
	public long getRID();
	public String getUrl();
	public String getMetodo();
	public int getParamsCount();
	public String getParam(final String NomeParam);
	public int getParamAsInt(final String NomeParam);
	public double getParamAsDouble(final String NomeParam);
	public int getQueryCount();
	public String getQuery(final String Nome);
	public String getQuery(final String nome, final String defaultValue);
	public int getQueryAsInt(final String Nome);
	public int getQueryAsInt(final String nome, int defaultValue);
	public double getQueryAsDouble(final String Nome);
	public double getQueryAsDouble(final String nome, double defaultValue);
	public String getModulo();
	public String getFunction();
	public OtpErlangObject getOtpRequest();
	public Date getParamAsDate(final String NomeParam) throws ParseException;
	public String getPayload();
	public <T> T getObject(Class<T> classOfObj, EmsJsonModelAdapter jsonModelAdapter);	
	public <T> T getObject(Class<T> classOfObj);
	public Map<String, Object> getObject();
	public Object getProperty(final String nome);
	public Object getProperty(final String nome, final Object defaultValue);
	public void setProperty(final String nome, final Object value);
	public Object mergeObjectFromPayload(Object obj);
	public Object mergeObjectFromPayload(Object obj, EmsJsonModelAdapter jsonModelAdapter);
	public Map<String, Object> getPayloadAsMap();
	public List<Map<String, Object>> getPayloadAsList();
	public <T> T getPayloadAsArray(Class<T> classOfArray);
	public <T> List<T> getPayloadAsList(Class<T[]> classOfArray);
	public Map<String, Object> getClient();
	public Map<String, Object> getUser();
	public Map<String, Object> getCatalog();
	public String getContentType();
	public String getScope();
	public String getAccessToken();
	public long getT1();
	public long getTimeout();
	public boolean isPostOrUpdateRequest();
}