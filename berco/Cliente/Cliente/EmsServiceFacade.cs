using System;
namespace br.erlangms
{
	public abstract class EmsServiceFacade : IEmsServiceFacade
	{
		private EmsConnection connection = null;
		private DaemonThread daemon = null;
		public enum States { BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN };
		private States state;

		public void initialize()
		{
			state = States.BEFORESTARTED;
			Class <? extends EmsServiceFacade > cls = getClass();
			connection = new EmsConnection(cls.getSimpleName(), cls.getName(), this);
			daemon = new DaemonThread(connection);
			daemon.start();
			state = States.STARTED;
		}

		public void terminate()
		{
			state = States.SHUTTINGDOWN;
			daemon.stop();
			connection.close();
			connection = null;
		}

		protected States getState()
		{
			return state;
		}

		protected EmsConnection getConnection()
		{
			return connection;
		}

		private class DaemonThread : Thread
		{

			private EmsConnection agent;

			public DaemonThread(EmsConnection agent) : base(agent)
			{
				this.agent = agent;
			}

			public void run()

			{
				try
				{
					agent.start();
				}
				catch (Exception e)
				{
					Console.WriteLine(e.ToString());
				}
			}
		}
	}
}