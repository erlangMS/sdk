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

public final class EmsServiceStream {
	private String from_url;
	private Map<String, Object> queries;
	private String response;

	/*
	 * Bug corrigido: o bloco static anterior desabilitava a validação SSL de toda a
	 * JVM
	 * (setDefaultSSLSocketFactory e setDefaultHostnameVerifier globais), afetando
	 * todas
	 * as conexões HTTPS do processo — inclusive de outras bibliotecas.
	 * Agora o SSLContext permissivo é criado e aplicado apenas por conexão, no
	 * método request().
	 */

	public EmsServiceStream() {
		this.from_url = null;
		this.queries = new java.util.HashMap<>();
		this.response = null;
	}

	public EmsServiceStream from(final String url) {
		if (url == null || url.isEmpty())
			throw new EmsValidationException(
					"Parâmetro do método EmsServiceStream.from(final String url) não pode ser nulo.");
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
			throw new EmsValidationException("EmsServiceStream não conseguiu criar a url " + restUrl);
		}
		URLConnection con = null;
		try {
			con = url.openConnection();
			// Bug corrigido: SSLContext permissivo aplicado apenas nesta conexão
			// específica,
			// não mais como default global da JVM.
			if (con instanceof HttpsURLConnection) {
				HttpsURLConnection httpsCon = (HttpsURLConnection) con;
				try {
					TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}

						public void checkClientTrusted(X509Certificate[] certs, String authType) {
						}

						public void checkServerTrusted(X509Certificate[] certs, String authType) {
						}
					} };
					// Bug corrigido: verificar sc != null antes de usar para evitar NPE
					SSLContext sc = SSLContext.getInstance("SSL");
					if (sc != null) {
						sc.init(null, trustAllCerts, new java.security.SecureRandom());
						httpsCon.setSSLSocketFactory(sc.getSocketFactory());
					}
					httpsCon.setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					});
				} catch (NoSuchAlgorithmException | KeyManagementException e) {
					e.printStackTrace();
					// Continua sem SSL customizado — usa o padrão da JVM
				}
			}
			con.setRequestProperty(EmsUtil.properties.authorizationHeaderName,
					EmsUtil.properties.authorizationHeaderValue);
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			throw new EmsValidationException("EmsServiceStream não conseguiu criar a conexão da url " + restUrl);
		}
		try {
			this.response = EmsUtil.readFullyAsString(con.getInputStream(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			throw new EmsValidationException("EmsServiceStream não conseguiu ler o response da url " + restUrl);
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
