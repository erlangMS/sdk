using System;
namespace br.erlangms
{
	public class EmsResponse
	{
		public EmsResponse(int code, String content)
		{
			this.code = code;
			this.content = content;
		}
		public int code;
		public String content;
	}
}

