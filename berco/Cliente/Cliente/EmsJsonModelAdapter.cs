using System;
namespace br.erlangms
{
	public interface EmsJsonModelAdapter
	{

		public Object findById(Class<T> classOfModel, int id);

	}
}

