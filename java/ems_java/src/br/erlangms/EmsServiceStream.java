package br.erlangms;

import java.util.List;
import java.util.Map;

public class EmsServiceStream {
	private String msg;
	private Map<String, Object> queries;
	private String response;
	
	public EmsServiceStream(){
		this.msg = null;
		this.queries = new java.util.HashMap<>();
		this.response = null;
	}
	
	public EmsServiceStream from(final String msg){
		if (msg == null || msg.isEmpty()) 
			throw new EmsValidationException("Parâmetro from do EmsServiceStream.from não pode ser nulo.");
		this.msg = msg;
		return this;
	}

	public EmsServiceStream setParameter(final Integer value) {
		if (value == null) 
			throw new EmsValidationException("Parâmetro value do EmsServiceStream.setParameter não pode ser nulo.");
		msg = msg.replaceFirst(":id", value.toString());
		return this;
	}

	public EmsServiceStream setQuery(final String key, final Object value) {
		this.queries.put(key, value);
		return this;
	}

	public EmsServiceStream request() {
		this.response = EmsUtil.getRestStream()
					   		.target(EmsConnection.getMsbusHost() + msg)
					   		.request("application/json")
					   		.header(EmsConnection.getAuthorizationHeaderName(), EmsConnection.getAuthorizationHeaderValue())
					   		.get(String.class);
		return this;
	}

	public <T> List<T> toList(final Class<T> classOfModel) {
		return EmsUtil.fromListJson(response.toString(), classOfModel, null);
	}

	@SuppressWarnings("unchecked")
	public List<Object> toList() {
		return (List<Object>) EmsUtil.fromJson(response.toString(), List.class);
	}

	public <T> T getObject(Class<T> classOfModel) {
		return (T) EmsUtil.fromJson(response, classOfModel);
	}

	public Object getObject() {
		return response;
	}
}	

