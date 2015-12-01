/*********************************************************************
 * @title Interface IEmsRequest
 * @version 1.0.0
 * @doc Interface que representa uma requisição para um serviço
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 ********************/
  
package br.erlangms;

import java.text.ParseException;
import java.util.Date;
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
	public int getQueryAsInt(final String Nome);
	public double getQueryAsDouble(final String Nome);
	public String getModulo();
	public String getFunction();
	public OtpErlangObject getOtpRequest();
	public Date getParamAsDate(final String NomeParam) throws ParseException;
	public String getPayload();
	public <T> T getObject(Class<T> classOfObj, EmsJsonModelAdapter jsonModelAdapter);	
	public <T> T getObject(Class<T> classOfObj);
	public Map<String, Object> getObject();
	public Object getProperty(final String nome);
	public Integer getPropertyAsInt(final String nome);
	public Object mergeObjectFromPayload(Object obj);
	public Object mergeObjectFromPayload(Object obj, EmsJsonModelAdapter jsonModelAdapter);
	public Map<String, Object> getPayloadAsMap();
	public String getContentType();
}