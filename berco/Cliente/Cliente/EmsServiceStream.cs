using System;
namespace br.erlangms
{
	public class EmsServiceStream
	{
		private String msg;
		private Map<String, Object> queries;
		private String response;

		public EmsServiceStream()
		{
			this.msg = null;
			this.queries = new java.util.HashMap<>();
			this.response = null;
		}

		public EmsServiceStream from(String msg)
		{
			this.msg = msg;
			return this;
		}

		public EmsServiceStream setParameter(int value)
		{
			msg = msg.replaceFirst(":id", value.ToString());
			return this;
		}

		public EmsServiceStream setQuery(String key, Object value)
		{
			this.queries.put(key, value);
			return this;
		}

		public EmsServiceStream request()
		{
			this.response = EmsUtil.getRestStream()
								   .target(EmsConnection.getMsbusHost() + msg)
								   .request("application/json")
								   .header(EmsConnection.getAuthorizationHeaderName(), EmsConnection.getAuthorizationHeaderValue())
								   .get(String.class);
		return this;
	}

	public <T> List<T> toList(Class<T> classOfModel)
	{
		System.out.println(response);
		return EmsUtil.fromListJson(response.toString(), classOfModel, null);
	}

	public List<Object> toList()
	{
		System.out.println(response);
		return (List<Object>)EmsUtil.fromJson(response.toString(), List.class);
	}

	public <T> T getObject(Class<T> classOfModel)
	{
		return (T)EmsUtil.fromJson(response, classOfModel);
	}

	public Object getObject()
	{
		return response;
	}

	}
}


