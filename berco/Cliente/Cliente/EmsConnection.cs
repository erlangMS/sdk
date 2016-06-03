using System;
using Erlang.NET;
using log4net;

namespace br.erlangms
{
	public class EmsConnection
	{
		private static int maxThreadPool;
		private static String cookie;
		private static String msbusHost;
		private static String hostName;
		private static String nodeName;
		private String nomeAgente;
		private String nomeService;
		private static OtpErlangBinary result_ok; 
		private static readonly ILog logger = LogManager.GetLogger(typeof(EmsConnection));
		private IEmsServiceFacade facade;
		private static OtpErlangPid dispatcherPid;
		private static String nodeUser;
		private static String nodePassword;
		private static String authorizationHeaderName;
		private static String authorizationHeaderValue;
		private OtpNode myNode = null;
		private OtpMbox myMbox = null;


		public EmsConnection(String nomeAgente, String nomeService, IEmsServiceFacade facade){
			byte[] result_ok_str = System.Text.Encoding.UTF8.GetBytes ("{\"ok\":\"ok\"}");
			result_ok = new OtpErlangBinary(result_ok_str);
			getSystemProperties();
			this.nomeAgente = nomeAgente;
			this.nomeService = nomeService;
			this.facade = facade;
		}

		/**
	 * Obtem as configurações necessárias para executar os agentes
	 * Exemplo: 
	 *    -Dcookie=erlangms
	 *    -Dems_node=node01
	 *    -Dems_msbus=http://localhost:2301
	 *    -Dems_cookie=erlangms
	 *    -Dems_max_thread_pool_by_agent=10
	 *    -Dems_user=xxxxxxx 
	 *    -Dems_password=xxxxxx 
	 * @param from pid do agente
	 * @return OtpErlangTuple
	 * @author Everton de Vargas Agilar
	 */
		private static void getSystemProperties() {
			String tmp_thread_pool = System.getProperty("ems_thread_pool");
			if (tmp_thread_pool != null){
				try{
					maxThreadPool = Integer.parseInt(tmp_thread_pool);
				}catch (NumberFormatException e){
					maxThreadPool = 12;
				}
			}else{
				maxThreadPool = 128;
			}
			String tmp_cookie = System.getProperty("ems_cookie");
			if (tmp_cookie != null){
				cookie = tmp_cookie;
			}else{
				cookie = "erlangms";
			}
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				Console.WriteLine("Não foi possível obter o hostname da máquina onde está o node.");
			}
			String tmp_nodeName = System.getProperty("ems_node");
			if (tmp_nodeName != null){
				nodeName = tmp_nodeName;
			}else{
				nodeName = "node01";
			}
			String tmp_msbusHost = System.getProperty("ems_msbus");
			if (tmp_msbusHost != null){
				msbusHost = tmp_msbusHost;
			}else{
				msbusHost = "http://localhost:2301";
			}
			String tmp_user = System.getProperty("ems_user");
			if (tmp_user != null){
				nodeUser = tmp_user;
			}else{
				nodeUser = "";
			}
			String tmp_password = System.getProperty("ems_password");
			if (tmp_password != null){
				nodePassword = tmp_password;
			}else{
				nodePassword = "";
			}

			String usernameAndPassword = nodeUser + ":" + nodePassword;
			authorizationHeaderName = "Authorization";
			authorizationHeaderValue = "Basic " + java.util.Base64.getEncoder()
				.encodeToString(usernameAndPassword.getBytes());

		}
	}
}

