using System;
using Erlang.NET;

namespace br.erlangms
{
	public interface IEmsRequest
	{
		public long getRID();
		public string getUrl();
		public string getMetodo();
		public int getParamsCount();
		public string getParam(String NomeParam);
		public int getParamAsInt(String NomeParam);
		public double getParamAsDouble(String NomeParam);
		public int getQueryCount();
		public string getQuery(String Nome);
		public int getQueryAsInt(String Nome);
		public double getQueryAsDouble(String Nome);
		public string getModulo();
		public string getFunction();
		public OtpErlangObject getOtpRequest();
		public DateTime getParamAsDate(String NomeParam) throws ParseException;
		public String getPayload();
		public <T> T getObject(Class<T> classOfObj, EmsJsonModelAdapter jsonModelAdapter);
		public <T> T getObject(Class<T> classOfObj);
		public Map<String, Object> getObject();
		public Object getProperty(String nome);
		public Integer getPropertyAsInt(String nome);
		public Object mergeObjectFromPayload(Object obj);
		public Object mergeObjectFromPayload(Object obj, EmsJsonModelAdapter jsonModelAdapter);
		public Map<String, Object> getPayloadAsMap();
		public String getContentType();
	}
}

