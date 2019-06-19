/*********************************************************************
 * @title ErlangMSApplication
 * @version 1.0.0
 * @doc Classe principal do SDK ErlangMS
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.unb.erlangms;

import java.util.ArrayList;
import java.util.List;

public class ErlangMSApplication{

	private static final List<EmsConnection> listServices = new ArrayList<>();

	public static void startService(final Object service) {
		String classNameOfService = service.getClass().getName();
		//logger.info("EmsServiceScan start "+ classNameOfService);
		EmsConnection connection1 = new EmsConnection(service, classNameOfService, false);
		connection1.start();
		listServices.add(connection1);
		if (EmsUtil.properties.isLinux) {
			EmsConnection connection2 = new EmsConnection(service, classNameOfService + "02", true);
			connection2.start();
			listServices.add(connection2);
		}
	}

	private void stopServices() {
		for (EmsConnection connection : listServices) {
			try {
				connection.close();
				connection.interrupt();
			}catch (Exception e) {
				// não faz nada
			}
		}
	}



}