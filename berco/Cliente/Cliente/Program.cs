using System;

using Erlang.NET;
using log4net;
using log4net.Config;

namespace Cliente
{
	class MainClass
	{
		// Define a static logger variable so that it references the
		// Logger instance named "MyApp".
		private static readonly ILog log = LogManager.GetLogger(typeof(MainClass));

		public static void Main (string[] args)
		{
			// Set up a simple configuration that logs on the console.
			BasicConfigurator.Configure();

			log.Info("Entering application.");

			System.Console.WriteLine("Cliente CSharp ErlangMS");

			OtpSelf cNode = new OtpSelf("clientnode", "aula");
			OtpPeer sNode = new OtpPeer("no1@puebla");

			OtpConnection connection = cNode.connect(sNode);

			OtpErlangObject[] msg = new OtpErlangObject[] { 
				new OtpErlangLong(2), new OtpErlangLong(5)
			}; 

			connection.sendRPC("mathserver", "multiply", msg);

			OtpErlangLong sum = (OtpErlangLong)connection.receiveRPC();
			Console.WriteLine("Return Value:" + sum.ToString());

			log.Info("Exiting application.");

		}
	}
}
