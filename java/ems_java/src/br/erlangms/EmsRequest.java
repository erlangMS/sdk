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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangRangeException;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class EmsRequest implements IEmsRequest {
	private OtpErlangTuple otp_request = null;
	private static OtpErlangAtom undefined = new OtpErlangAtom("undefined");
	private Map<String, Object> properties = null;
	private int queryCount = -1;
	private long rid = 0L;
	private long timeout = 0L;
	private long t1 = 0L;
	private boolean isPostOrUpdateRequestFlag = false;
	private String method = null;
	private String url = null;
	private Map<String, Object> userJson = null;
	private Map<String, Object> clientJson = null;
	private String contentType = null;
	private String modulo = null;
	private String function = null;
	private String payload = null;
	private int paramCount = 0;
	private String access_token;
	private String scope;

	public EmsRequest(final OtpErlangTuple otp_request){
		setOtpRequest(otp_request);
	}

	public EmsRequest(){
	}
	
	public void setOtpRequest(final OtpErlangTuple otp_request) {
		this.otp_request = otp_request;
		this.properties = null;
		this.queryCount = -1;
		this.rid = ((OtpErlangLong)otp_request.elementAt(0)).longValue();
		this.timeout = ((OtpErlangLong)otp_request.elementAt(14)).longValue(); 
		this.t1 = ((OtpErlangLong)otp_request.elementAt(13)).longValue();
		this.method = ((OtpErlangString)otp_request.elementAt(2)).stringValue();
		this.url = ((OtpErlangString)otp_request.elementAt(1)).stringValue();
		this.isPostOrUpdateRequestFlag = method.equals("POST") || method.equals("PUT");
		this.contentType = new String(((OtpErlangBinary)otp_request.elementAt(6)).binaryValue());
		this.modulo = ((OtpErlangString)otp_request.elementAt(7)).stringValue();
		this.function = ((OtpErlangString)otp_request.elementAt(8)).stringValue();
		this.payload = new String(((OtpErlangBinary)otp_request.elementAt(5)).binaryValue());
		this.paramCount = ((OtpErlangMap)otp_request.elementAt(3)).arity();
		this.userJson = null;
		this.clientJson = null;
		OtpErlangObject OAuth2FieldObj = otp_request.elementAt(12);
		if (OAuth2FieldObj != null && OAuth2FieldObj instanceof OtpErlangTuple) {
			OtpErlangTuple OAuth2Field = (OtpErlangTuple) OAuth2FieldObj;
			if (OAuth2Field != null) {
				this.scope = new String(((OtpErlangBinary)OAuth2Field.elementAt(1)).binaryValue());
				this.access_token = new String(((OtpErlangBinary)OAuth2Field.elementAt(1)).binaryValue());
			}
		}else {
			this.scope = "";
			this.access_token = "";
		}
	}
	
	/**
	 * Retorna o Request Identifier (RID) do request.
	 * @return Request Identifier (RID) do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getRID(){
		return rid;
	}

	/**
	 * Retorna a url do request.
	 * @return url do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getUrl(){
		return url;
	}
	
	/**
	 * Retorna o método do request (GET, POST, PUT, DELETE)
	 * @return String GET, POST, PUT, DELETE
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getMetodo(){
		return method;
	}

	/**
	 * Retorna a quantidade de parâmetros do request.
	 * @return a quantidade de parâmetros do request
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getParamsCount(){
		return paramCount;
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * @param nome do parâmetro
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getParam(final String nome) {
		if (nome == null){
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getParam.");
		}
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
			throw new EmsValidationException("Não foi possível obter o parâmetro "+ nome + " do request.");
		}
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * @param nome do parâmetro
	 * @return valor da querystring como inteiro
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getParamAsInt(final String nome) {
		OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
		OtpErlangBinary OtpNomeParam = new OtpErlangBinary(nome.getBytes());
		OtpErlangLong otp_result = (OtpErlangLong) params.get(OtpNomeParam);
		try {
			return otp_result.intValue();
		} catch (OtpErlangRangeException e) {
			throw new EmsValidationException("Parâmetro "+ nome + " não é inteiro.");				
		}
	}
	
	/**
	 * Retorna um parâmetro do request pelo nome.
	 * @param nome do parâmetro
	 * @return valor da querystring como double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getParamAsDouble(final String nome) {
		try{
			return Double.parseDouble(getParam(nome));
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter o parâmetro "+ nome + " no tipo double do request.");
		}
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * @param nome do parâmetro
	 * @return valor da querystring como Date
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Date getParamAsDate(final String nome) throws ParseException {
		try{
			return new SimpleDateFormat("dd/mm/yyyy").parse(getParam(nome));
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter o parâmetro "+ nome + " no tipo Date do request.");
		}
	}

	/**
	 * Retorna a quantidade de querystrings do request.
	 * @return quantidade de querystrings do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryCount(){
		if (queryCount  != -1){
			return queryCount;
		}
		try{
			OtpErlangObject Querystring = otp_request.elementAt(4);
			if (!Querystring.equals(undefined)){
				queryCount = ((OtpErlangMap) Querystring).arity(); 
			}else{
				queryCount = 0;
			}
			return queryCount;
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível obter a quantidade de queries do request.");
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
		if (nome == null){
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getQuery.");
		}
		if (getQueryCount() > 0){
			try{
				OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
				OtpErlangBinary OtpNome = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
				if (otp_result != null){
					String result = new String(otp_result.binaryValue(), "ISO-8859-1");
					return result;
				}else{
					return null;
				}
			}catch (Exception e){
				throw new EmsValidationException("Não foi possível obter a query "+ nome + " do request.");
			}
		}else{
			throw new EmsValidationException("Não existe a query " + nome + " do request.");
		}
	}

	/**
	 * Retorna uma querystring pelo nome ou um valor default se não informado no request.
	 * @param nome da querystring
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getQuery(final String nome, final String defaultValue) {
		if (nome == null){
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getQuery.");
		}
		if (getQueryCount() > 0){
			try{
				OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
				OtpErlangBinary OtpNome = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
				if (otp_result != null){
					String result = new String(otp_result.binaryValue());
					return result;
				}else{
					return defaultValue;
				}
			}catch (Exception e){
				throw new EmsValidationException("Não foi possível obter a query "+ nome + " do request.");
			}
		}else{
			throw new EmsValidationException("Não existe a query " + nome + " do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como int. Um erro será gerado se não for possível retornar um int.
	 * @param nome nome da querystring
	 * @return valor int
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryAsInt(final String nome) {
		try{
			return Integer.parseInt(getQuery(nome));
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter a query "+ nome + " para int do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como int ou o valor default se não existir. 
	 * Um erro será gerado se não for possível retornar um int.
	 * @param nome nome da querystring
	 * @return valor int
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryAsInt(final String nome, int defaultValue) {
		try{
			String result = getQuery(nome);
			if (result != null){
				return Integer.parseInt(result);
			}else{
				return defaultValue;
			}
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter a query "+ nome + " para int do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como Double. 
	 * Um erro será gerado se não for possível retornar um double.
	 * @param nome nome da querystring
	 * @return valor double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getQueryAsDouble(final String nome) {
		try{
			return Double.parseDouble(getQuery(nome));
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter a query "+ nome + " para double do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como Double ou o valor default se não existir. 
	 * Um erro será gerado se não for possível retornar um double.
	 * @param nome nome da querystring
	 * @return valor double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getQueryAsDouble(final String nome, double defaultValue) {
		try{
			String result = getQuery(nome);
			if (result != null){
				return Double.parseDouble(result);
			}else{
				return defaultValue;
			}
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter a query "+ nome + " para double do request.");
		}
	}

	/**
	 * Retorna o payload do request como texto. Geralmente será a string JSON.
	 * @return String do payload
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getPayload(){
		return payload;
	}

	/**
	 * Retorna o payload do request serializado como objeto. Um erro será gerado se não for possível ler o objeto JSON.
	 * Útil para converter o objeto JSON do request no objeto que será trabalhado na camada de negócio
	 * @param classOfObj classe do objeto que será serializado. Exemplo: Municipio.class
	 * @param <T> classe do objeto que será serializado. Exemplo: Municipio.class
	 * @return Object
	 * @author Everton de Vargas Agilar
	 */
	public <T> T getObject(final Class<T> classOfObj){
		return getObject(classOfObj, null);
	}
	
	@Override
	public <T> T getObject(final Class<T> classOfObj, final EmsJsonModelAdapter jsonModelAdapter) {
		try{
			return EmsUtil.fromJson(getPayload(), classOfObj, jsonModelAdapter);
		}catch (Exception e){
			throw new EmsValidationException(e.getMessage());
		}
	}
	
	@Override
	public Map<String, Object> getObject() {
		return getPayloadAsMap();
	}
	
	/**
	 * Permite obter uma propriedade incluída pelo desenvolvedor.
	 * @param nome nome da propriedade
	 * @return Object 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object getProperty(final String nome){
		if (nome == null){
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getProperty.");
		}
		if (properties == null){
			throw new EmsValidationException("Propriedade "+ nome + " não existe na requisição.");
		}
		if (properties.containsKey(nome)){
			return properties.get(nome);
		}else{
			throw new EmsValidationException("Propriedade "+ nome + " não existe na requisição."); 
		}
	};

	/**
	 * Permite obter uma propriedade incluída pelo desenvolvedor. Se não existe a proprieadade, retorna o defaultValue.
	 * @param nome nome da propriedade
	 * @param defaultValue valor default
	 * @return Object 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object getProperty(final String nome, final Object defaultValue){
		if (nome == null){
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getProperty.");
		}
		if (properties == null){
			throw new EmsValidationException("Propriedade "+ nome + " não existe na requisição.");
		}
		return properties.getOrDefault(nome, defaultValue);
	};

	/**
	 * Permite ao desenvolvedor definir uma propriedade e armazenar na requisição.
	 * @param nome nome da propriedade
	 * @param value valor do objeto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public void setProperty(final String nome, final Object value){
		if (nome == null){
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.setProperty.");
		}
		if (properties == null){
			properties = new java.util.HashMap<String, Object>();
		}
		properties.put(nome, value);
	};
	
	/**
	 * Retorna o payload do request como map. Um erro será gerado se não for possível ler o objeto JSON.
	 * @return map
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPayloadAsMap(){
		try{
			return (Map<String, Object>) EmsUtil.fromJson(getPayload(), HashMap.class);
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter o payload do request em um objeto da interface java.util.Map. Erro interno: "+ e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getPayloadAsList(){
		try{
			return (List<Map<String, Object>>) EmsUtil.fromJson(getPayload(), List.class);
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter o payload do request em um objeto da interface java.util.List. Erro interno: "+ e.getMessage());
		}
	}
	
	/**
	 * Retorna o payload do request como um array de objetos
	 * @param classOfArray classe do array para serializar. Ex. Usuario[].class
	 * @return list
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public <T> T getPayloadAsArray(Class<T> classOfArray) {
		try{
			String payload = getPayload();
			return EmsUtil.gson.fromJson(payload, classOfArray);
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível converter o payload do request em uma lista de objetos. Erro interno: "+ e.getMessage());
		}
	}
	
	/**
	 * Retorna o payload do request como uma lista de objetos
	 * @param classOfArray classe do array para serializar. Ex. Usuario[].class
	 * @return list
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public <T> List<T> getPayloadAsList(Class<T[]> classOfArray) {
		T[] result = getPayloadAsArray(classOfArray);
		return Arrays.asList(result);
	}
	
	/**
	 * Retorna o ContentType do request.
	 * @return ContentType do request
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getContentType(){
		return contentType;
	}

	/**
	 * Retorna o nome do módulo do contrato de serviço.
	 * @return nome do módulo do contrato de serviço
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getModulo(){
		return modulo;
	}

	/**
	 * Retorna o nome da função do contrato de serviço.
	 * @return nome da função do contrato de serviço
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getFunction(){
		return function;
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
	 * Realiza o merge dos atributos do objeto com o objeto JSON do request
	 * Útil para métodos que fazem o update dos dados no banco de dados
	 * @param obj Objeto para fazer merge com o payload
	 * @return objeto após merge
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object mergeObjectFromPayload(final Object obj) {
		return mergeObjectFromPayload(obj, null);
	}

	/**
	 * Realiza o merge dos atributos do objeto com o objeto JSON do request.
	 * Útil para métodos que fazem o update dos dados no banco de dados
	 * @param obj Objeto para fazer merge com o payload
	 * @return objeto após merge
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object mergeObjectFromPayload(final Object obj, final EmsJsonModelAdapter emsJsonModelSerialize) {
		if (obj != null){
			final Map<String, Object> update_values = (Map<String, Object>) getObject(HashMap.class);
			EmsUtil.setValuesFromMap(obj, update_values, emsJsonModelSerialize);
			return obj;
		}else{
			return null;
		}
	}


	/**
	 * Obter o cliente do request.
	 * @return map com atributo/valor 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getClient() {
		if (clientJson == null) {
			try{
				String clientJsonString = new String(((OtpErlangBinary)otp_request.elementAt(9)).binaryValue());
				clientJson = (Map<String, Object>) EmsUtil.fromJson(clientJsonString, HashMap.class);
			}catch (Exception e){
				throw new EmsValidationException("Não foi possível obter o client do request. Erro interno: "+ e.getMessage());
			}
		}
		return clientJson;
	}
	
	/**
	 * Obter o usuário do request.
	 * @return map com atributo/valor 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getUser() {
		if (userJson == null) {
			try{
				String userJsonString = new String(((OtpErlangBinary)otp_request.elementAt(10)).binaryValue());
				userJson = (Map<String, Object>) EmsUtil.fromJson(userJsonString, HashMap.class);
			}catch (Exception e){
				throw new EmsValidationException("Não foi possível obter o user do request. Erro interno: "+ e.getMessage());
			}
		}
		return userJson;
	}

	/**
	 * Obter o catálogo do request.
	 * @return map com atributo/valor 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getCatalog() {
		try{
			String catalogJson = new String(((OtpErlangBinary)otp_request.elementAt(11)).binaryValue());
			return (Map<String, Object>) EmsUtil.fromJson(catalogJson, HashMap.class);
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível obter o catálogo do request. Erro interno: "+ e.getMessage());
		}
	}

	/**
	 * Obter o scopo oauth2 do request.
	 * @return oauth2 scope 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getScope() {
		return scope;
	}

	/**
	 * Obter access_token do request.
	 * @return oauth2 access token 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getAccessToken() {
		return access_token;
	}

	/**
	 * Obter o T1 do request.
	 * @return long 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getT1() {
		return t1;
	}

	/**
	 * Obter o timeout do request.
	 * @return int 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getTimeout() {
		return timeout;
	}

	/**
	 * Is POST ou PUT request
	 * @return boolean 
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public boolean isPostOrUpdateRequest() {
		return isPostOrUpdateRequestFlag;
	}


}
