package br.erlangms.samples.service;

import java.util.List;
import java.util.Map;

import br.erlangms.EmsServiceFacade;
import br.erlangms.EmsUtil;

public abstract class EmsServiceProxy extends EmsServiceFacade {

	public class EmsServiceStream {
		private EmsServiceProxy proxy; 
		private String msg;
		private Map<String, Object> queries;
		private String response;
		
		public EmsServiceStream(EmsServiceProxy proxy){
			this.proxy = proxy;
			this.msg = null;
			this.queries = new java.util.HashMap<>();
			this.response = null;
		}
		
		public EmsServiceStream from(final String msg){
			this.msg = msg;
			return this;
		}

		public EmsServiceStream setParameter(Integer value) {
			msg = msg.replaceFirst(":id", value.toString());
			return this;
		}

		public EmsServiceStream setQuery(String key, Object value) {
			this.queries.put(key, value);
			return this;
		}

		public EmsServiceStream request() {
			//OtpNode node = proxy.getNode();
			//OtpMbox mbox = proxy.getMBox();
			String host = "http://localhost:2301";

			String username = "everton";
	        String password = "123456";
	 
	        String usernameAndPassword = username + ":" + password;
	        String authorizationHeaderName = "Authorization";
	        String authorizationHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString( usernameAndPassword.getBytes() );
	 
	        // Build the form for a post request
	 			
			this.response = EmsUtil.getRestStream()
						   		.target(host + msg)
						   		.request("application/json")
						   		.header(authorizationHeaderName, authorizationHeaderValue)
						   		.get(String.class);
			
			//OtpErlangObject request = null;

			
			/*request = EmsUtil.serializeObjectToErlangRequest("Ola mundo");			
			mbox.send(EmsAgent.getDispatcherPid(), request);

			request = EmsUtil.serializeObjectToErlangRequest("teste2");			
			mbox.send(EmsAgent.getDispatcherPid(), request);

			List<String> lista = new ArrayList();
			lista.add("everton");
			lista.add("agilar");
			
			request = EmsUtil.serializeObjectToErlangRequest(lista, mbox.self());			
			mbox.send(EmsAgent.getDispatcherPid(), request);

	       OtpErlangObject myObject = null;
	       OtpErlangTuple myMsg;
	       OtpErlangTuple otp_request;
			
            try {
				myObject = mbox.receive();
			} catch (OtpErlangExit | OtpErlangDecodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            myMsg = (OtpErlangTuple) myObject;
            otp_request = (OtpErlangTuple) myMsg.elementAt(0);
            //EmsRequest req = new EmsRequest(otp_request);
            */
			

			return this;
		}

		public <T> List<T> toList(Class<T> classOfModel) {
			System.out.println(response);
			return EmsUtil.fromListJson(response.toString(), classOfModel, null);
		}

		@SuppressWarnings("unchecked")
		public List<Object> toList() {
			System.out.println(response);
			return (List<Object>) EmsUtil.fromJson(response.toString(), List.class);
		}

		public <T> T getObject(Class<T> classOfModel) {
			return (T) EmsUtil.fromJson(response, classOfModel);
		}

		public Object getObject() {
			return response;
		}
	}	
	
	public EmsServiceStream getStream(){
		return new EmsServiceStream(this);
	}

}
