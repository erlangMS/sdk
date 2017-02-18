package br.erlangms;

import java.util.List;
import java.util.Map;

public class EmsServiceStream {
	private String url;
	private Map<String, Object> queries;
	private String response;
	
	public EmsServiceStream(){
		this.url = null;
		this.queries = new java.util.HashMap<>();
		this.response = null;
	}
	
	public EmsServiceStream from(final String url){
		if (url == null || url.isEmpty()) 
			throw new EmsValidationException("Parâmetro do método EmsServiceStream.from(final String url) não pode ser nulo.");
		this.url = url;
		return this;
	}

	public EmsServiceStream setParameter(final Integer value) {
		if (value == null) 
			throw new EmsValidationException("Parâmetro value do EmsServiceStream.setParameter não pode ser nulo.");
		url = url.replaceFirst(":id", value.toString());
		return this;
	}

	public EmsServiceStream setQuery(final String key, final Object value) {
		this.queries.put(key, value);
		return this;
	}

	public EmsServiceStream request() {
		this.response = EmsUtil.getRestStream()
					   		.target(EmsUtil.properties.ESB_URL + url)
					   		.request("application/json")
					   		.header(EmsUtil.properties.authorizationHeaderName, EmsUtil.properties.authorizationHeaderValue)
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

