/*********************************************************************
 * @title Módulo EmsRequest
 * @version 1.0.0
 * @doc Classe que representa uma requisição para um serviço
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class EmsRequest implements IEmsRequest {
	private OtpErlangTuple otp_request;
	private static OtpErlangAtom undefined = new OtpErlangAtom("undefined");

	public EmsRequest(final OtpErlangTuple otp_request){
		this.otp_request = otp_request;
	}
	
	@Override
	public long getRID(){
		return ((OtpErlangLong)otp_request.elementAt(0)).longValue();
	}

	@Override
	public String getUrl(){
		return ((OtpErlangString)otp_request.elementAt(1)).stringValue();
	}
	
	@Override
	public String getMetodo(){
		return ((OtpErlangString)otp_request.elementAt(2)).stringValue();
	}

	@Override
	public int getParamsCount(){
		try{
			OtpErlangObject Params = otp_request.elementAt(3);
			if (!Params.equals(undefined)){
				return ((OtpErlangMap) Params).arity();
			}else{
				return 0;
			}
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível obter a quantidade de parâmetros do request.");
		}
	}
	
	@Override
	public String getParam(final String nome) {
		try{
			if (getParamsCount() > 0){
				OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
				OtpErlangBinary OtpNomeParam = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) params.get(OtpNomeParam);
				if (otp_result != null){
					String result = new String(otp_result.binaryValue());
					return result;
				}else{
					return null;
				}
			}else{
				return null;
			}
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível obter o parâmetro "+ nome + " do request.");
		}
	}

	@Override
	public int getParamAsInt(final String nome) {
		try{
			return Integer.parseInt(getParam(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter o parâmetro "+ nome + " no tipo int do request.");
		}
	}
	
	@Override
	public double getParamAsDouble(final String nome) {
		try{
			return Double.parseDouble(getParam(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter o parâmetro "+ nome + " no tipo double do request.");
		}
	}

	@Override
	public Date getParamAsDate(final String nome) throws ParseException {
		try{
			return new SimpleDateFormat("dd/mm/yyyy").parse(getParam(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter o parâmetro "+ nome + " no tipo Date do request.");
		}
	}

	@Override
	public int getQueryCount(){
		try{
			OtpErlangObject Querystring = otp_request.elementAt(4);
			if (!Querystring.equals(undefined)){
				return ((OtpErlangMap) Querystring).arity();
			}else{
				return 0;
			}
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível a quantidade de queries do request.");
		}
	}

	@Override
	public String getQuery(final String nome) {
		try{
			if (getQueryCount() > 0){
				OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
				OtpErlangBinary OtpNome = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
				if (otp_result != null){
					String result = new String(otp_result.binaryValue());
					return result;
				}else{
					return null;
				}
			}else{
				return null;
			}
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível obter a query "+ nome + " do request.");
		}
	}

	@Override
	public String getPayload(){
		return ((OtpErlangString)otp_request.elementAt(5)).stringValue();
	}

	public Object getObject(Class<?> clazz){
		try{
			return EmsUtil.fromJson(getPayload(), clazz);
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível serializar o objeto a partir do payload.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPayloadAsMap(){
		try{
			return (Map<String, Object>) EmsUtil.fromJson(getPayload(), HashMap.class);
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter o payload em map do request.");
		}
	}

	@Override
	public String getContentType(){
		return ((OtpErlangString)otp_request.elementAt(6)).stringValue();
	}

	@Override
	public String getModulo(){
		return ((OtpErlangString)otp_request.elementAt(7)).stringValue();
	}

	@Override
	public String getFunction(){
		return ((OtpErlangString)otp_request.elementAt(8)).stringValue();
	}

	@Override
	public OtpErlangObject getOtpRequest(){
		return this.otp_request;
	}

	@Override
	public int getQueryAsInt(String nome) {
		try{
			return Integer.parseInt(getQuery(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter a query "+ nome + " para int do request.");
		}
	}

	@Override
	public double getQueryAsDouble(String nome) {
		try{
			return Double.parseDouble(getQuery(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter a query "+ nome + " para double do request.");
		}
	}
	
}
