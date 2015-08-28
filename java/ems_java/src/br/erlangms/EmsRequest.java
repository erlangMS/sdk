/*********************************************************************
 * @title Módulo EmsRequest
 * @version 1.0.0
 * @doc Classe que representa uma requisição para um serviço
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class EmsRequest implements IEmsRequest {
	private OtpErlangTuple otp_request;
	private static OtpErlangAtom undefined = new OtpErlangAtom("undefined");

	public EmsRequest(final OtpErlangTuple otp_request){
		this.otp_request = otp_request;
	}
	
	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getRID()
	 */
	@Override
	public long getRID(){
		return ((OtpErlangLong)otp_request.elementAt(0)).longValue();
	}

	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getUrl()
	 */
	@Override
	public String getUrl(){
		return ((OtpErlangString)otp_request.elementAt(1)).stringValue();
	}
	
	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getMetodo()
	 */
	@Override
	public String getMetodo(){
		return ((OtpErlangString)otp_request.elementAt(2)).stringValue();
	}

	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getParamsCount()
	 */
	@Override
	public int getParamsCount(){
		OtpErlangObject Params = otp_request.elementAt(3);
		if (!Params.equals(undefined)){
			return ((OtpErlangMap) Params).arity();
		}else{
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getParam(java.lang.String)
	 */
	@Override
	public String getParam(final String NomeParam) {
		if (getParamsCount() > 0){
			OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
			OtpErlangBinary OtpNomeParam = new OtpErlangBinary(NomeParam.getBytes());
			OtpErlangBinary otp_result = (OtpErlangBinary) params.get(OtpNomeParam);
			if (otp_result != null){
				String result = new String(otp_result.binaryValue());
				return result;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getQueryCount()
	 */
	@Override
	public int getQueryCount(){
		OtpErlangObject Querystring = otp_request.elementAt(4);
		if (!Querystring.equals(undefined)){
			return ((OtpErlangMap) Querystring).arity();
		}else{
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getQuery(java.lang.String)
	 */
	@Override
	public String getQuery(final String Nome) {
		if (getQueryCount() > 0){
			OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
			OtpErlangBinary OtpNome = new OtpErlangBinary(Nome.getBytes());
			OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
			if (otp_result != null){
				String result = new String(otp_result.binaryValue());
				return result;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getModulo()
	 */
	@Override
	public String getModulo(){
		return ((OtpErlangString)otp_request.elementAt(5)).stringValue();
	}

	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getFunction()
	 */
	@Override
	public String getFunction(){
		return ((OtpErlangString)otp_request.elementAt(6)).stringValue();
	}

	/* (non-Javadoc)
	 * @see br.erlangms.IEmsRequest#getOtpRequest()
	 */
	@Override
	public OtpErlangObject getOtpRequest(){
		return this.otp_request;
	}
	
}
