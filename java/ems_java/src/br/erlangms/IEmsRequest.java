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

	public abstract long getRID();

	public abstract String getUrl();

	public abstract String getMetodo();

	public abstract int getParamsCount();

	public abstract String getParam(String NomeParam);

	public abstract int getQueryCount();

	public abstract String getQuery(String Nome);

	public abstract String getModulo();

	public abstract String getFunction();

	public abstract OtpErlangObject getOtpRequest();

	public int getParamAsInt(String NomeParam);

	public Double getParamAsDouble(String NomeParam);

	public Date getParamAsDate(String NomeParam) throws ParseException;

}