using System;
namespace br.erlangms
{
	public interface EmsJsonModelAdapter
	{

		Object findById(Type classOfModel, int id);

	}
}
