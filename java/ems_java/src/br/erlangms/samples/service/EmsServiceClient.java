package br.erlangms.samples.service;

import java.util.List;
import java.util.Map;

import br.erlangms.EmsServiceFacade;

import com.ericsson.otp.erlang.OtpErlangBinary;

public abstract class EmsServiceClient extends EmsServiceFacade {

	public class EmsServiceStream{
		private OtpErlangBinary msg;
		private List<Object> parameters;
		private Map<String, Object> queries;
		
		public EmsServiceStream(){
			this.msg = null;
		}
		
		public EmsServiceStream from(final String msg){
			this.msg = new OtpErlangBinary(((String) msg).getBytes());
			return this;
		}

		public EmsServiceStream setParameter(Integer i) {
			this.parameters.add(i);
			return this;
		}

		public EmsServiceStream setQuery(String key, Object value) {
			this.queries.put(key, value);
			return this;
		}

		public EmsServiceStream request() {
			return this;
		}

		public List<Object> toList() {
			return null;
			
		}

		public Object getObject(Class<?> classOfModel) {
			return null;
		}

		public Object getObject() {
			return null;
		}
		
	}
	
	public EmsServiceStream getStream(){
		return new EmsServiceStream();
	}

}
