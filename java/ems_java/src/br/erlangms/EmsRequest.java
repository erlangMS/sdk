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
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangRangeException;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class EmsRequest implements IEmsRequest {
	private OtpErlangTuple otp_request;
	private static OtpErlangAtom undefined = new OtpErlangAtom("undefined");
	private Map<String, Object> properties;

	public EmsRequest(final OtpErlangTuple otp_request){
		this.otp_request = otp_request;
		this.properties = null;
	}
	
	/**
	 * Retorna o Request Identifier (RID) do request.
	 * @return Request Identifier (RID) do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getRID(){
		return ((OtpErlangLong)otp_request.elementAt(0)).longValue();
	}

	/**
	 * Retorna a url do request.
	 * @return url do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getUrl(){
		return ((OtpErlangString)otp_request.elementAt(1)).stringValue();
	}
	
	/**
	 * Retorna o método do request (GET, POST, PUT, DELETE)
	 * @return String GET, POST, PUT, DELETE
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getMetodo(){
		return ((OtpErlangString)otp_request.elementAt(2)).stringValue();
	}

	/**
	 * Retorna a quantidade de parâmetros do request.
	 * @return a quantidade de parâmetros do request
	 * @author Everton de Vargas Agilar
	 */
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

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * @param nome do parâmetro
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getParam(final String nome) {
		try{
			if (getParamsCount() > 0){
				OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
				OtpErlangBinary OtpNomeParam = new OtpErlangBinary(nome.getBytes());
				OtpErlangLong otp_result = (OtpErlangLong) params.get(OtpNomeParam);
				if (otp_result != null){
					String result = Integer.toString(otp_result.intValue());
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
		OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
		OtpErlangBinary OtpNomeParam = new OtpErlangBinary(nome.getBytes());
		OtpErlangLong otp_result = (OtpErlangLong) params.get(OtpNomeParam);
		try {
			return otp_result.intValue();
		} catch (OtpErlangRangeException e) {
			throw new EmsRequestException("Parâmetro "+ nome + " não é inteiro.");				
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

	/**
	 * Retorna a quantidade de querystrings do request.
	 * @return quantidade de querystrings do request.
	 * @author Everton de Vargas Agilar
	 */
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
			throw new EmsRequestException("Não foi possível obter a quantidade de queries do request.");
		}
	}

	/**
	 * Retorna uma querystring pelo nome.
	 * @param nome da querystring
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getQuery(final String nome) {
		if (getQueryCount() > 0){
			try{
				OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
				OtpErlangBinary OtpNome = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
				if (otp_result != null){
					String result = new String(otp_result.binaryValue());
					return result;
				}else{
					return null;
				}
			}catch (Exception e){
				throw new EmsRequestException("Não foi possível obter a query "+ nome + " do request.");
			}
		}else{
			throw new EmsRequestException("Não existe nenhuma query para o request.");
		}
	}

	/**
	 * Retorna o payload do request como texto. Geralmente será a string JSON.
	 * @return String do payload
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getPayload(){
		return ((OtpErlangString)otp_request.elementAt(5)).stringValue();
	}

	/**
	 * Retorna o payload do request serializado como objeto. Um erro será gerado se não for possível ler o objeto JSON.
	 * Útil para converter o objeto JSON do request no objeto que será trabalhado na camada de negócio
	 * @param classOfObj classe do objeto que será serializado. Exemplo: Municipio.class
	 * @return Object
	 * @author Everton de Vargas Agilar
	 */
	public <T> T getObject(Class<T> classOfObj){
		return getObject(classOfObj, null);
	}
	
	@Override
	public <T> T getObject(Class<T> classOfObj, EmsJsonModelAdapter jsonModelAdapter) {
		try{
			return EmsUtil.fromJson(getPayload(), classOfObj, jsonModelAdapter);
		}catch (Exception e){
			throw new EmsRequestException(e.getMessage());
		}
	}
	
	@Override
	public Map<String, Object> getObject() {
		return getPayloadAsMap();
	}
	
	/**
	 * Permite obter uma propriedade do payload. Útil para tratar o payload como uma lista de propriedades.
	 * @param nome nome da propriedade
	 * @return Object 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object getProperty(final String nome){
		if (properties == null){
			properties = getPayloadAsMap();
		}
		Object result = properties.get(nome);
		if (result == null){
			throw new EmsValidationException("Propriedade "+ nome + " não exite na requisição."); 
		}
		return result;
	};
	
	@Override
	public Integer getPropertyAsInt(final String nome){
		Object prop = getProperty(nome);
		if (prop instanceof String){
			return Integer.parseInt((String) prop);
		}else if (prop instanceof Double){
			return ((Double) prop).intValue();
		}else{
			throw new EmsValidationException("Propriedade "+ nome + " não é numérica.");
		}
	};

	/**
	 * Retorna o payload do request como map. Um erro será gerado se não for possível ler o objeto JSON.
	 * @return Map<String, Object>
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPayloadAsMap(){
		try{
			return (Map<String, Object>) EmsUtil.fromJson(getPayload(), HashMap.class);
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter o payload do request em um objeto da interface java.util.Map. Erro interno: "+ e.getMessage());
		}
	}

	/**
	 * Retorna o ContentType do request.
	 * @return ContentType do request
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getContentType(){
		return ((OtpErlangString)otp_request.elementAt(6)).stringValue();
	}

	/**
	 * Retorna o nome do módulo do contrato de serviço.
	 * @return nome do módulo do contrato de serviço
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getModulo(){
		return ((OtpErlangString)otp_request.elementAt(7)).stringValue();
	}

	/**
	 * Retorna o nome da função do contrato de serviço.
	 * @return nome da função do contrato de serviço
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getFunction(){
		return ((OtpErlangString)otp_request.elementAt(8)).stringValue();
	}

	/**
	 * Retorna a estrutura interna do request. Não recomendado utilizar.
	 * @return OtpErlangObject
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public OtpErlangObject getOtpRequest(){
		return this.otp_request;
	}

	/**
	 * Retorna uma querystring do request como int. Um erro será gerado se não for possível retornar um int.
	 * @param nome nome da querystring
	 * @return valor int
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryAsInt(String nome) {
		try{
			return Integer.parseInt(getQuery(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter a query "+ nome + " para int do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como Double. Um erro será gerado se não for possível retornar um double.
	 * @param nome nome da querystring
	 * @return valor double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getQueryAsDouble(String nome) {
		try{
			return Double.parseDouble(getQuery(nome));
		}catch (Exception e){
			throw new EmsRequestException("Não foi possível converter a query "+ nome + " para double do request.");
		}
	}

	/**
	 * Realiza o merge dos atributos do objeto com o objeto JSON do request
	 * Útil para métodos que fazem o update dos dados no banco de dados
	 * @param obj Objeto para fazer merge com o payload
	 * @return objeto após merge
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object mergeObjectFromPayload(Object obj) {
		return mergeObjectFromPayload(obj, null);
	}

	/**
	 * Realiza o merge dos atributos do objeto com o objeto JSON do request
	 * Útil para métodos que fazem o update dos dados no banco de dados
	 * @param obj Objeto para fazer merge com o payload
	 * @return objeto após merge
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object mergeObjectFromPayload(Object obj, EmsJsonModelAdapter emsJsonModelSerialize) {
		if (obj != null){
			final Map<String, Object> update_values = (Map<String, Object>) getObject(HashMap.class);
			EmsUtil.setValuesFromMap(obj, update_values, emsJsonModelSerialize);
			return obj;
		}else{
			return null;
		}
	}

	
}
