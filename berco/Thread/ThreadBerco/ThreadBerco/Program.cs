using System;
using System.Threading;
            
namespace ThreadBerco
{
	class MainClass
	{


		public static void Main(string[] args)
		{
			Worker workerObject = new Worker();
			Thread workerThread = new Thread(workerObject.DoWork);

			workerThread.Start();

			while (!workerThread.IsAlive) ;
			Thread.Sleep(1);

			workerObject.RequestStop();

			workerThread.Join();

			Console.WriteLine("Main Thread: Worker thread terminou.");


		}


	}
}
