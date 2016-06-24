using System;
namespace br.erlangms
{
	public abstract class EmsServiceProxy : EmsServiceFacade
	{
		public EmsServiceStream getStream()
		{
			return new EmsServiceStream();
		}

	}
}

