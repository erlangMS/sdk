using System;
using System.Threading;

namespace ThreadBerco
{
	public class Worker
	{

		private volatile bool _shouldStop;


		public Worker()
		{
		}

		public void DoWork()
		{
			while (!_shouldStop)
			{
				Console.WriteLine("Worker thread: Iniciando...");
			}
			Console.WriteLine("worker thread: finalizado com sucesso");
		}

		public void RequestStop()
		{
			_shouldStop = true;
		}
	}
}