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

import com.ericsson.otp.erlang.OtpErlangObject;

public interface IEmsRequest {

	public long getRID();

	public String getUrl();

	public String getMetodo();

	public int getParamsCount();

	public String getParam(final String NomeParam);

	public int getQueryCount();

	public String getQuery(final String Nome);

	public String getModulo();

	public String getFunction();

	public OtpErlangObject getOtpRequest();

	public int getParamAsInt(final String NomeParam);

	public Double getParamAsDouble(final String NomeParam);

	public Date getParamAsDate(final String NomeParam) throws ParseException;

	public String getPayload();
	
	public Object getObject(Class<?> clazz);

	public String getContentType();

}