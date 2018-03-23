package br.erlangms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class EmsServiceStream {
	private String from_url;
	private Map<String, Object> queries;
	private String response;
	
	static {
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
		            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		            public void checkClientTrusted(X509Certificate[] certs, String authType) {
		            }
		            public void checkServerTrusted(X509Certificate[] certs, String authType) {
		            }
		        }
		    };
		
		    // Install the all-trusting trust manager
		    SSLContext sc = null;
			try {
				sc = SSLContext.getInstance("SSL");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    try {
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		    // Create all-trusting host name verifier
		    HostnameVerifier allHostsValid = new HostnameVerifier() {
		        public boolean verify(String hostname, SSLSession session) {
		            return true;
		        }
		    };
		
		    // Install the all-trusting host verifier
		    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	public EmsServiceStream(){
		this.from_url = null;
		this.queries = new java.util.HashMap<>();
		this.response = null;
	}
	
	public EmsServiceStream from(final String url){
		if (url == null || url.isEmpty()) 
			throw new EmsValidationException("Parâmetro do método EmsServiceStream.from(final String url) não pode ser nulo.");
		this.from_url = url;
		return this;
	}

	public EmsServiceStream setParameter(final Integer value) {
		if (value == null) 
			throw new EmsValidationException("Parâmetro value do EmsServiceStream.setParameter não pode ser nulo.");
		from_url = from_url.replaceFirst(":id", value.toString());
		return this;
	}

	public EmsServiceStream setQuery(final String key, final Object value) {
		this.queries.put(key, value);
		return this;
	}

    
	public EmsServiceStream request() {
		String restUrl = EmsUtil.properties.ESB_URL + from_url;
		URL url = null;
        try {
			url = new URL(restUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new EmsValidationException("EmsServiceStream não conseguiu criar a url "+ restUrl);
		}
        URLConnection con = null;
		try {
			con = url.openConnection();
			con.setRequestProperty(EmsUtil.properties.authorizationHeaderName, EmsUtil.properties.authorizationHeaderValue);
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			throw new EmsValidationException("EmsServiceStream não conseguiu criar a conexão da url "+ restUrl);
		}
        try {
        	this.response = EmsUtil.readFullyAsString(con.getInputStream(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			throw new EmsValidationException("EmsServiceStream não conseguiu ler o response da url "+ restUrl);
		}
	    
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

