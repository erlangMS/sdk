using System;
using Erlang.NET;
using System.Collections.Generic;

namespace br.erlangms
{
	public interface IEmsRequest<T>
	{
		long getRID();
		string getUrl();
		string getMetodo();
		int getParamsCount();
		string getParam(String NomeParam);
		int getParamAsInt(String NomeParam);
		double getParamAsDouble(String NomeParam);
		int getQueryCount();
		string getQuery(String Nome);
		int getQueryAsInt(String Nome);
		double getQueryAsDouble(String Nome);
		string getModulo();
		string getFunction();
		OtpErlangObject getOtpRequest();
		DateTime getParamAsDate(String NomeParam);
		String getPayload();
		T getObject(Type classOfObj, EmsJsonModelAdapter jsonModelAdapter);
		T getObject(Type classOfObj);
		Dictionary<String,Object> getObject();
		Object getProperty(String nome);
		int getPropertyAsInt(String nome);
		Object mergeObjectFromPayload(Object obj);
		Object mergeObjectFromPayload(Object obj, EmsJsonModelAdapter jsonModelAdapter);
		Dictionary<String, Object> getPayloadAsMap();
		String getContentType();
	}
}

