/*********************************************************************
 * @title Módulo EmsRequest
 * @version 1.0.0
 * @doc Classe que representa uma requisição para um serviço
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangRangeException;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class EmsRequest implements IEmsRequest {
	private OtpErlangTuple otp_request = null;
	private static OtpErlangAtom undefined = new OtpErlangAtom("undefined");
	private Map<String, Object> properties = null;
	private int queryCount = -1;
	private long rid = 0L;
	private long timeout = 0L;
	private long t1 = 0L;
	private boolean isPostOrUpdateRequestFlag = false;
	private String method = null;
	private String url = null;
	private Map<String, Object> userJson = null;
	private Map<String, Object> clientJson = null;
	private String contentType = null;
	private String modulo = null;
	private String function = null;
	private String payload = null;
	private int paramCount = 0;
	private String access_token;
	private String scope;

	public EmsRequest(final OtpErlangTuple otp_request) {
		setOtpRequest(otp_request);
	}

	public EmsRequest() {
	}

	public void setOtpRequest(final OtpErlangTuple otp_request) {
		EmsUtil.logger.info("========== EmsRequest recebido do barramento ==========");

		// Log dos tipos recebidos para debug
		// EmsUtil.logger.info("Estrutura da mensagem (" + otp_request.arity() + "
		// elementos):");
		// for (int i = 0; i < otp_request.arity(); i++) {
		// OtpErlangObject elem = otp_request.elementAt(i);
		// String typeName = elem != null ? elem.getClass().getSimpleName() : "null";
		// EmsUtil.logger.info(String.format(" [%d]: %s", i, typeName));
		// }

		this.otp_request = otp_request;
		this.properties = null;
		this.queryCount = -1;

		// Extrai valores long
		this.rid = extractLongValue(otp_request.elementAt(0), "rid");
		this.timeout = extractLongValue(otp_request.elementAt(14), "timeout");
		this.t1 = extractLongValue(otp_request.elementAt(13), "t1");

		// Extrai campos com suporte a múltiplos tipos (binary, string, list)
		this.method = extractStringValue(otp_request.elementAt(2), "method");
		this.url = extractStringValue(otp_request.elementAt(1), "url");
		this.isPostOrUpdateRequestFlag = method.equals("POST") || method.equals("PUT");

		// Extrai campos binários como string UTF-8
		this.contentType = extractBinaryAsString(otp_request.elementAt(6), "contentType");

		// Payload requer tratamento especial com logging detalhado em caso de erro
		try {
			this.payload = extractBinaryAsString(otp_request.elementAt(5), "payload");
		} catch (EmsValidationException e) {
			// Loga a estrutura completa do payload para debug
			EmsUtil.logger.severe("Erro ao extrair payload. Estrutura recebida:");
			logOtpStructure(otp_request.elementAt(5), "payload", 0);
			throw e;
		}

		this.modulo = extractStringValue(otp_request.elementAt(7), "modulo");
		this.function = extractStringValue(otp_request.elementAt(8), "function");

		// Extrai mapa de parâmetros
		OtpErlangMap paramsMap = extractMapValue(otp_request.elementAt(3), "params");
		this.paramCount = paramsMap.arity();
		this.userJson = null;
		this.clientJson = null;

		// Processa OAuth2
		OtpErlangTuple OAuth2Field = extractTupleValue(otp_request.elementAt(12), "oauth2");
		if (OAuth2Field != null) {
			this.scope = extractBinaryAsString(OAuth2Field.elementAt(0), "oauth2.scope");
			this.access_token = extractBinaryAsString(OAuth2Field.elementAt(1), "oauth2.access_token");
		} else {
			this.scope = "";
			this.access_token = "";
		}

		// Logging detalhado para debug
		EmsUtil.logger.info("RID: " + this.rid);
		EmsUtil.logger.info("URL: " + this.url);
		EmsUtil.logger.info("Método: " + this.method);
		EmsUtil.logger.info("Módulo: " + this.modulo);
		EmsUtil.logger.info("Função: " + this.function);
		EmsUtil.logger.info("ContentType: " + this.contentType);
		EmsUtil.logger.info("Timeout: " + this.timeout + "ms");
		EmsUtil.logger.info("T1: " + this.t1);

		// Loga parâmetros
		if (this.paramCount > 0) {
			EmsUtil.logger.info("Parâmetros (" + this.paramCount + "):");
			try {
				OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
				for (OtpErlangObject key : params.keys()) {
					String keyStr = new String(((OtpErlangBinary) key).binaryValue());
					OtpErlangObject value = params.get(key);
					String valueStr = value.toString();
					EmsUtil.logger.info("  " + keyStr + " = " + valueStr);
				}
			} catch (Exception e) {
				EmsUtil.logger.warning("Erro ao logar parâmetros: " + e.getMessage());
			}
		} else {
			EmsUtil.logger.info("Parâmetros: nenhum");
		}

		// Loga querystrings
		try {
			OtpErlangObject Querystring = otp_request.elementAt(4);
			if (!Querystring.equals(undefined)) {
				OtpErlangMap queries = ((OtpErlangMap) Querystring);
				int queryCount = queries.arity();
				EmsUtil.logger.info("Querystrings (" + queryCount + "):");
				for (OtpErlangObject key : queries.keys()) {
					String keyStr = extractBinaryAsString(key, "querystring.key");
					OtpErlangObject value = queries.get(key);
					String valueStr = extractBinaryAsString(value, "querystring.value");
					EmsUtil.logger.info("  " + keyStr + " = " + valueStr);
				}
			} else {
				EmsUtil.logger.info("Querystrings: nenhuma");
			}
		} catch (Exception e) {
			EmsUtil.logger.warning("Erro ao logar querystrings: " + e.getMessage());
		}

		// Loga payload
		if (this.payload != null && !this.payload.isEmpty()) {
			// Loga o payload bruto truncado
			String payloadLog = this.payload.length() > 500
					? this.payload.substring(0, 500) + "... (truncado, total: " + this.payload.length() + " chars)"
					: this.payload;
			EmsUtil.logger.info("Payload (" + this.payload.length() + " chars): " + payloadLog);
		} else {
			EmsUtil.logger.info("Payload: vazio");
		}

		// Loga informações de autenticação
		if (!this.scope.isEmpty() || !this.access_token.isEmpty()) {
			// EmsUtil.logger.info("OAuth2 Scope: " + this.scope);
			EmsUtil.logger.info("OAuth2 Access Token: " + (this.access_token.isEmpty() ? "vazio" : "***presente***"));
		} else {
			EmsUtil.logger.info("OAuth2: não autenticado");
		}

		EmsUtil.logger.info("======================================================");
	}

	/**
	 * Extrai um valor string de um OtpErlangObject que pode ser OtpErlangBinary,
	 * OtpErlangString ou OtpErlangList (lista de inteiros representando
	 * caracteres).
	 * Este método garante compatibilidade com diferentes versões do barramento.
	 * 
	 * @param obj       Objeto Erlang a ser convertido
	 * @param fieldName Nome do campo (para mensagens de erro)
	 * @return String extraída do objeto
	 * @throws EmsValidationException se o tipo não for suportado
	 */
	private String extractStringValue(OtpErlangObject obj, String fieldName) {
		if (obj == null) {
			throw new EmsValidationException("Campo " + fieldName + " não pode ser null.");
		}

		try {
			// Tenta como OtpErlangString (tipo esperado originalmente)
			if (obj instanceof OtpErlangString) {
				return ((OtpErlangString) obj).stringValue();
			}

			// Tenta como OtpErlangBinary (tipo enviado pelo barramento para alguns campos)
			if (obj instanceof OtpErlangBinary) {
				return new String(((OtpErlangBinary) obj).binaryValue());
			}

			// Tenta como OtpErlangList (tipo enviado quando usa binary_to_list no Erlang)
			if (obj instanceof OtpErlangList) {
				return otpListToString((OtpErlangList) obj);
			}

			// Tipo não suportado
			throw new EmsValidationException(
					"Campo " + fieldName + " possui tipo não suportado: " + obj.getClass().getName() +
							". Tipos suportados: OtpErlangString, OtpErlangBinary, OtpErlangList.");

		} catch (Exception e) {
			if (e instanceof EmsValidationException) {
				throw e;
			}
			throw new EmsValidationException(
					"Erro ao extrair valor string do campo " + fieldName + ": " + e.getMessage());
		}
	}

	/**
	 * Converte uma OtpErlangList (lista de inteiros representando códigos ASCII)
	 * em uma String Java. Este método é necessário porque o Erlang binary_to_list/1
	 * converte binários em listas de inteiros.
	 * 
	 * @param list Lista Erlang contendo códigos de caracteres
	 * @return String construída a partir dos códigos de caracteres
	 */
	private String otpListToString(OtpErlangList list) {
		if (list == null || list.arity() == 0) {
			return "";
		}

		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.arity(); i++) {
				OtpErlangObject element = list.elementAt(i);
				if (element instanceof OtpErlangLong) {
					int charCode = ((OtpErlangLong) element).intValue();
					sb.append((char) charCode);
				} else {
					throw new EmsValidationException(
							"Elemento da lista não é um inteiro: " + element.getClass().getName());
				}
			}
			return sb.toString();
		} catch (OtpErlangRangeException e) {
			throw new EmsValidationException(
					"Erro ao converter lista Erlang para string: valor fora do range de int. " + e.getMessage());
		}
	}

	/**
	 * Extrai um valor binário de um OtpErlangBinary e converte para String UTF-8.
	 * Este método garante que a conversão seja feita com o charset correto para
	 * suportar caracteres especiais, acentos e emojis.
	 * Também aceita OtpErlangList para compatibilidade com payloads vazios.
	 * 
	 * @param obj       Objeto Erlang a ser convertido (deve ser OtpErlangBinary ou
	 *                  OtpErlangList)
	 * @param fieldName Nome do campo (para mensagens de erro)
	 * @return String extraída do binário com charset UTF-8
	 * @throws EmsValidationException se o objeto não for OtpErlangBinary/List ou
	 *                                houver
	 *                                erro na conversão
	 */
	private String extractBinaryAsString(OtpErlangObject obj, String fieldName) {
		if (obj == null) {
			throw new EmsValidationException("Campo " + fieldName + " não pode ser null.");
		}

		// Aceita OtpErlangBinary (caso mais comum)
		if (obj instanceof OtpErlangBinary) {
			try {
				// Converte binary para string usando UTF-8
				return new String(((OtpErlangBinary) obj).binaryValue(), "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				// UTF-8 sempre está disponível na JVM, mas tratamos por segurança
				// Fallback para charset padrão
				return new String(((OtpErlangBinary) obj).binaryValue());
			} catch (Exception e) {
				throw new EmsValidationException(
						"Erro ao extrair valor binário do campo " + fieldName + ": " + e.getMessage());
			}
		}

		// Aceita OtpErlangList (para payloads vazios ou quando o barramento envia como
		// lista)
		if (obj instanceof OtpErlangList) {
			OtpErlangList list = (OtpErlangList) obj;

			// Lista vazia
			if (list.arity() == 0) {
				return "";
			}

			// Verifica o tipo do primeiro elemento
			OtpErlangObject firstElem = list.elementAt(0);
			String firstElemType = firstElem.getClass().getSimpleName();

			// Se for lista de inteiros (códigos ASCII), converte
			if (firstElem instanceof OtpErlangLong) {
				return otpListToString(list);
			}

			// Se não for lista de inteiros, é uma estrutura complexa não suportada
			throw new EmsValidationException(
					"Campo " + fieldName + " é uma lista com " + list.arity() + " elementos, mas contém " +
							"elementos do tipo " + firstElemType + " em vez de inteiros (códigos ASCII). " +
							"Isso indica que o barramento está enviando uma estrutura complexa não suportada. " +
							"Verifique o dispatcher Erlang.");
		}

		// Aceita OtpErlangAtom (para undefined ou outros atoms)
		if (obj instanceof OtpErlangAtom) {
			OtpErlangAtom atom = (OtpErlangAtom) obj;
			// Se for undefined, retorna string vazia
			if (atom.atomValue().equals("undefined")) {
				return "";
			}
			// Para outros atoms, retorna o valor como string
			return atom.atomValue();
		}

		// Tipo não suportado
		String actualType = obj.getClass().getSimpleName();
		throw new EmsValidationException(
				"Campo " + fieldName + " possui tipo não suportado: " + actualType + ". " +
						"Tipos esperados: OtpErlangBinary, OtpErlangList (de inteiros) ou OtpErlangAtom.");
	}

	/**
	 * Loga a estrutura de um objeto Erlang para debug.
	 * Útil para entender estruturas complexas recebidas do barramento.
	 * 
	 * @param obj       Objeto Erlang a ser inspecionado
	 * @param fieldName Nome do campo
	 * @param depth     Profundidade atual (para indentação)
	 */
	private void logOtpStructure(OtpErlangObject obj, String fieldName, int depth) {
		if (depth > 3) {
			return; // Limite de profundidade para evitar loops infinitos
		}

		// Cria indentação manualmente (Java 8 não tem String.repeat)
		StringBuilder indentBuilder = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			indentBuilder.append("  ");
		}
		String indent = indentBuilder.toString();
		String type = obj != null ? obj.getClass().getSimpleName() : "null";

		EmsUtil.logger.severe(indent + fieldName + ": " + type);

		if (obj instanceof OtpErlangTuple) {
			OtpErlangTuple tuple = (OtpErlangTuple) obj;
			EmsUtil.logger.severe(indent + "  (tupla com " + tuple.arity() + " elementos)");
			for (int i = 0; i < tuple.arity(); i++) {
				logOtpStructure(tuple.elementAt(i), "elem[" + i + "]", depth + 1);
			}
		} else if (obj instanceof OtpErlangList) {
			OtpErlangList list = (OtpErlangList) obj;
			EmsUtil.logger.severe(indent + "  (lista com " + list.arity() + " elementos)");
			int max = Math.min(list.arity(), 5); // Mostra apenas os 5 primeiros
			for (int i = 0; i < max; i++) {
				logOtpStructure(list.elementAt(i), "item[" + i + "]", depth + 1);
			}
			if (list.arity() > 5) {
				EmsUtil.logger.severe(indent + "  ... (mais " + (list.arity() - 5) + " itens)");
			}
		} else if (obj instanceof OtpErlangBinary) {
			OtpErlangBinary bin = (OtpErlangBinary) obj;
			int size = bin.binaryValue().length;
			String preview = size > 50 ? " (primeiros 50 bytes)" : "";
			EmsUtil.logger.severe(indent + "  (binary com " + size + " bytes" + preview + ")");
		} else if (obj instanceof OtpErlangLong) {
			try {
				EmsUtil.logger.severe(indent + "  (valor: " + ((OtpErlangLong) obj).longValue() + ")");
			} catch (Exception e) {
				EmsUtil.logger.severe(indent + "  (erro ao ler valor)");
			}
		} else if (obj instanceof OtpErlangAtom) {
			EmsUtil.logger.severe(indent + "  (atom: " + obj.toString() + ")");
		}
	}

	/**
	 * Extrai um valor long de um OtpErlangLong.
	 * 
	 * @param obj       Objeto Erlang a ser convertido (deve ser OtpErlangLong)
	 * @param fieldName Nome do campo (para mensagens de erro)
	 * @return Valor long extraído
	 * @throws EmsValidationException se o objeto não for OtpErlangLong ou houver
	 *                                erro na conversão
	 */
	private long extractLongValue(OtpErlangObject obj, String fieldName) {
		if (obj == null) {
			throw new EmsValidationException("Campo " + fieldName + " não pode ser null.");
		}

		if (!(obj instanceof OtpErlangLong)) {
			throw new EmsValidationException(
					"Campo " + fieldName + " deve ser OtpErlangLong, mas é: " + obj.getClass().getName());
		}

		try {
			return ((OtpErlangLong) obj).longValue();
		} catch (Exception e) {
			throw new EmsValidationException(
					"Erro ao extrair valor long do campo " + fieldName + ": " + e.getMessage());
		}
	}

	/**
	 * Extrai um OtpErlangMap.
	 * 
	 * @param obj       Objeto Erlang a ser convertido (deve ser OtpErlangMap)
	 * @param fieldName Nome do campo (para mensagens de erro)
	 * @return OtpErlangMap extraído
	 * @throws EmsValidationException se o objeto não for OtpErlangMap
	 */
	private OtpErlangMap extractMapValue(OtpErlangObject obj, String fieldName) {
		if (obj == null) {
			throw new EmsValidationException("Campo " + fieldName + " não pode ser null.");
		}

		if (!(obj instanceof OtpErlangMap)) {
			throw new EmsValidationException(
					"Campo " + fieldName + " deve ser OtpErlangMap, mas é: " + obj.getClass().getName());
		}

		return (OtpErlangMap) obj;
	}

	/**
	 * Extrai um OtpErlangTuple.
	 * 
	 * @param obj       Objeto Erlang a ser convertido (deve ser OtpErlangTuple)
	 * @param fieldName Nome do campo (para mensagens de erro)
	 * @return OtpErlangTuple extraído, ou null se o objeto for undefined
	 * @throws EmsValidationException se o objeto não for OtpErlangTuple ou
	 *                                undefined
	 */
	private OtpErlangTuple extractTupleValue(OtpErlangObject obj, String fieldName) {
		if (obj == null) {
			return null;
		}

		// Permite undefined (retorna null)
		if (obj instanceof OtpErlangAtom && obj.equals(undefined)) {
			return null;
		}

		if (!(obj instanceof OtpErlangTuple)) {
			throw new EmsValidationException(
					"Campo " + fieldName + " deve ser OtpErlangTuple ou undefined, mas é: " + obj.getClass().getName());
		}

		return (OtpErlangTuple) obj;
	}

	/**
	 * Retorna o Request Identifier (RID) do request.
	 * 
	 * @return Request Identifier (RID) do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getRID() {
		return rid;
	}

	/**
	 * Retorna a url do request.
	 * 
	 * @return url do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * Retorna o método do request (GET, POST, PUT, DELETE)
	 * 
	 * @return String GET, POST, PUT, DELETE
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getMetodo() {
		return method;
	}

	/**
	 * Retorna a quantidade de parâmetros do request.
	 * 
	 * @return a quantidade de parâmetros do request
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getParamsCount() {
		return paramCount;
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * 
	 * @param nome do parâmetro
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getParam(final String nome) {
		if (nome == null) {
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getParam.");
		}
		try {
			if (getParamsCount() > 0) {
				OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
				OtpErlangBinary OtpNomeParam = new OtpErlangBinary(nome.getBytes());
				OtpErlangLong otp_result = (OtpErlangLong) params.get(OtpNomeParam);
				if (otp_result != null) {
					String result = Integer.toString(otp_result.intValue());
					return result;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new EmsValidationException("Não foi possível obter o parâmetro " + nome + " do request.");
		}
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * 
	 * @param nome do parâmetro
	 * @return valor da querystring como inteiro
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getParamAsInt(final String nome) {
		OtpErlangMap params = ((OtpErlangMap) otp_request.elementAt(3));
		OtpErlangBinary OtpNomeParam = new OtpErlangBinary(nome.getBytes());
		OtpErlangLong otp_result = (OtpErlangLong) params.get(OtpNomeParam);
		try {
			return otp_result.intValue();
		} catch (OtpErlangRangeException e) {
			throw new EmsValidationException("Parâmetro " + nome + " não é inteiro.");
		}
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * 
	 * @param nome do parâmetro
	 * @return valor da querystring como double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getParamAsDouble(final String nome) {
		try {
			return Double.parseDouble(getParam(nome));
		} catch (Exception e) {
			throw new EmsValidationException(
					"Não foi possível converter o parâmetro " + nome + " no tipo double do request.");
		}
	}

	/**
	 * Retorna um parâmetro do request pelo nome.
	 * 
	 * @param nome do parâmetro
	 * @return valor da querystring como Date
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Date getParamAsDate(final String nome) throws ParseException {
		try {
			return new SimpleDateFormat("dd/mm/yyyy").parse(getParam(nome));
		} catch (Exception e) {
			throw new EmsValidationException(
					"Não foi possível converter o parâmetro " + nome + " no tipo Date do request.");
		}
	}

	/**
	 * Retorna a quantidade de querystrings do request.
	 * 
	 * @return quantidade de querystrings do request.
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryCount() {
		if (queryCount != -1) {
			return queryCount;
		}
		try {
			OtpErlangObject Querystring = otp_request.elementAt(4);
			if (!Querystring.equals(undefined)) {
				queryCount = ((OtpErlangMap) Querystring).arity();
			} else {
				queryCount = 0;
			}
			return queryCount;
		} catch (Exception e) {
			throw new EmsValidationException("Não foi possível obter a quantidade de queries do request.");
		}
	}

	/**
	 * Retorna uma querystring pelo nome.
	 * 
	 * @param nome da querystring
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getQuery(final String nome) {
		if (nome == null) {
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getQuery.");
		}
		if (getQueryCount() > 0) {
			try {
				OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
				OtpErlangBinary OtpNome = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
				if (otp_result != null) {
					String result = new String(otp_result.binaryValue());
					return result;
				} else {
					return null;
				}
			} catch (Exception e) {
				throw new EmsValidationException("Não foi possível obter a query " + nome + " do request.");
			}
		} else {
			throw new EmsValidationException("Não existe a query " + nome + " do request.");
		}
	}

	/**
	 * Retorna uma querystring pelo nome ou um valor default se não informado no
	 * request.
	 * 
	 * @param nome da querystring
	 * @return valor da querystring como texto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getQuery(final String nome, final String defaultValue) {
		if (nome == null) {
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getQuery.");
		}
		if (getQueryCount() > 0) {
			try {
				OtpErlangMap Queries = ((OtpErlangMap) otp_request.elementAt(4));
				OtpErlangBinary OtpNome = new OtpErlangBinary(nome.getBytes());
				OtpErlangBinary otp_result = (OtpErlangBinary) Queries.get(OtpNome);
				if (otp_result != null) {
					String result = new String(otp_result.binaryValue(), "ISO-8859-1");
					return result;
				} else {
					return defaultValue;
				}
			} catch (Exception e) {
				throw new EmsValidationException("Não foi possível obter a query " + nome + " do request.");
			}
		} else {
			throw new EmsValidationException("Não existe a query " + nome + " do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como int. Um erro será gerado se não for
	 * possível retornar um int.
	 * 
	 * @param nome nome da querystring
	 * @return valor int
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryAsInt(final String nome) {
		try {
			return Integer.parseInt(getQuery(nome));
		} catch (Exception e) {
			throw new EmsValidationException("Não foi possível converter a query " + nome + " para int do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como int ou o valor default se não
	 * existir.
	 * Um erro será gerado se não for possível retornar um int.
	 * 
	 * @param nome nome da querystring
	 * @return valor int
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public int getQueryAsInt(final String nome, int defaultValue) {
		try {
			String result = getQuery(nome);
			if (result != null) {
				return Integer.parseInt(result);
			} else {
				return defaultValue;
			}
		} catch (Exception e) {
			throw new EmsValidationException("Não foi possível converter a query " + nome + " para int do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como Double.
	 * Um erro será gerado se não for possível retornar um double.
	 * 
	 * @param nome nome da querystring
	 * @return valor double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getQueryAsDouble(final String nome) {
		try {
			return Double.parseDouble(getQuery(nome));
		} catch (Exception e) {
			throw new EmsValidationException("Não foi possível converter a query " + nome + " para double do request.");
		}
	}

	/**
	 * Retorna uma querystring do request como Double ou o valor default se não
	 * existir.
	 * Um erro será gerado se não for possível retornar um double.
	 * 
	 * @param nome nome da querystring
	 * @return valor double
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public double getQueryAsDouble(final String nome, double defaultValue) {
		try {
			String result = getQuery(nome);
			if (result != null) {
				return Double.parseDouble(result);
			} else {
				return defaultValue;
			}
		} catch (Exception e) {
			throw new EmsValidationException("Não foi possível converter a query " + nome + " para double do request.");
		}
	}

	/**
	 * Retorna o payload do request como texto. Geralmente será a string JSON.
	 * 
	 * @return String do payload
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getPayload() {
		return payload;
	}

	/**
	 * Retorna o payload do request serializado como objeto. Um erro será gerado se
	 * não for possível ler o objeto JSON.
	 * Útil para converter o objeto JSON do request no objeto que será trabalhado na
	 * camada de negócio
	 * 
	 * @param classOfObj classe do objeto que será serializado. Exemplo:
	 *                   Municipio.class
	 * @param <T>        classe do objeto que será serializado. Exemplo:
	 *                   Municipio.class
	 * @return Object
	 * @author Everton de Vargas Agilar
	 */
	public <T> T getObject(final Class<T> classOfObj) {
		return getObject(classOfObj, null);
	}

	@Override
	public <T> T getObject(final Class<T> classOfObj, final EmsJsonModelAdapter jsonModelAdapter) {
		try {
			return EmsUtil.fromJson(getPayload(), classOfObj, jsonModelAdapter);
		} catch (Exception e) {
			throw new EmsValidationException(e.getMessage());
		}
	}

	@Override
	public Map<String, Object> getObject() {
		return getPayloadAsMap();
	}

	/**
	 * Permite obter uma propriedade incluída pelo desenvolvedor.
	 * 
	 * @param nome nome da propriedade
	 * @return Object
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object getProperty(final String nome) {
		if (nome == null) {
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getProperty.");
		}
		if (properties == null) {
			throw new EmsValidationException("Propriedade " + nome + " não existe na requisição.");
		}
		if (properties.containsKey(nome)) {
			return properties.get(nome);
		} else {
			throw new EmsValidationException("Propriedade " + nome + " não existe na requisição.");
		}
	};

	/**
	 * Permite obter uma propriedade incluída pelo desenvolvedor. Se não existe a
	 * proprieadade, retorna o defaultValue.
	 * 
	 * @param nome         nome da propriedade
	 * @param defaultValue valor default
	 * @return Object
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object getProperty(final String nome, final Object defaultValue) {
		if (nome == null) {
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.getProperty.");
		}
		if (properties == null) {
			throw new EmsValidationException("Propriedade " + nome + " não existe na requisição.");
		}
		return properties.getOrDefault(nome, defaultValue);
	};

	/**
	 * Permite ao desenvolvedor definir uma propriedade e armazenar na requisição.
	 * 
	 * @param nome  nome da propriedade
	 * @param value valor do objeto
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public void setProperty(final String nome, final Object value) {
		if (nome == null) {
			throw new EmsValidationException("Propriedade nome não pode ser null para EmsRequest.setProperty.");
		}
		if (properties == null) {
			properties = new java.util.HashMap<String, Object>();
		}
		properties.put(nome, value);
	};

	/**
	 * Retorna o payload do request como map. Um erro será gerado se não for
	 * possível ler o objeto JSON.
	 * 
	 * @return map
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPayloadAsMap() {
		try {
			return (Map<String, Object>) EmsUtil.fromJson(getPayload(), HashMap.class);
		} catch (Exception e) {
			throw new EmsValidationException(
					"Não foi possível converter o payload do request em um objeto da interface java.util.Map. Erro interno: "
							+ e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getPayloadAsList() {
		try {
			return (List<Map<String, Object>>) EmsUtil.fromJson(getPayload(), List.class);
		} catch (Exception e) {
			throw new EmsValidationException(
					"Não foi possível converter o payload do request em um objeto da interface java.util.List. Erro interno: "
							+ e.getMessage());
		}
	}

	/**
	 * Retorna o payload do request como um array de objetos
	 * 
	 * @param classOfArray classe do array para serializar. Ex. Usuario[].class
	 * @return list
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public <T> T getPayloadAsArray(Class<T> classOfArray) {
		try {
			String payload = getPayload();
			return EmsUtil.gson.fromJson(payload, classOfArray);
		} catch (Exception e) {
			throw new EmsValidationException(
					"Não foi possível converter o payload do request em uma lista de objetos. Erro interno: "
							+ e.getMessage());
		}
	}

	/**
	 * Retorna o payload do request como uma lista de objetos
	 * 
	 * @param classOfArray classe do array para serializar. Ex. Usuario[].class
	 * @return list
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public <T> List<T> getPayloadAsList(Class<T[]> classOfArray) {
		T[] result = getPayloadAsArray(classOfArray);
		return Arrays.asList(result);
	}

	/**
	 * Retorna o ContentType do request.
	 * 
	 * @return ContentType do request
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * Retorna o nome do módulo do contrato de serviço.
	 * 
	 * @return nome do módulo do contrato de serviço
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getModulo() {
		return modulo;
	}

	/**
	 * Retorna o nome da função do contrato de serviço.
	 * 
	 * @return nome da função do contrato de serviço
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getFunction() {
		return function;
	}

	/**
	 * Retorna a estrutura interna do request. Não recomendado utilizar.
	 * 
	 * @return OtpErlangObject
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public OtpErlangObject getOtpRequest() {
		return this.otp_request;
	}

	/**
	 * Realiza o merge dos atributos do objeto com o objeto JSON do request
	 * Útil para métodos que fazem o update dos dados no banco de dados
	 * 
	 * @param obj Objeto para fazer merge com o payload
	 * @return objeto após merge
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public Object mergeObjectFromPayload(final Object obj) {
		return mergeObjectFromPayload(obj, null);
	}

	/**
	 * Realiza o merge dos atributos do objeto com o objeto JSON do request.
	 * Útil para métodos que fazem o update dos dados no banco de dados
	 * 
	 * @param obj Objeto para fazer merge com o payload
	 * @return objeto após merge
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object mergeObjectFromPayload(final Object obj, final EmsJsonModelAdapter emsJsonModelSerialize) {
		if (obj != null) {
			final Map<String, Object> update_values = (Map<String, Object>) getObject(HashMap.class);
			EmsUtil.setValuesFromMap(obj, update_values, emsJsonModelSerialize);
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * Obter o cliente do request.
	 * 
	 * @return map com atributo/valor
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getClient() {
		if (clientJson == null) {
			try {
				String clientJsonString = new String(((OtpErlangBinary) otp_request.elementAt(9)).binaryValue());
				clientJson = (Map<String, Object>) EmsUtil.fromJson(clientJsonString, HashMap.class);
			} catch (Exception e) {
				throw new EmsValidationException(
						"Não foi possível obter o client do request. Erro interno: " + e.getMessage());
			}
		}
		return clientJson;
	}

	/**
	 * Obter o usuário do request.
	 * 
	 * @return map com atributo/valor
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getUser() {
		if (userJson == null) {
			try {
				String userJsonString = new String(((OtpErlangBinary) otp_request.elementAt(10)).binaryValue());
				userJson = (Map<String, Object>) EmsUtil.fromJson(userJsonString, HashMap.class);
			} catch (Exception e) {
				throw new EmsValidationException(
						"Não foi possível obter o user do request. Erro interno: " + e.getMessage());
			}
		}
		return userJson;
	}

	/**
	 * Obter o catálogo do request.
	 * 
	 * @return map com atributo/valor
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getCatalog() {
		try {
			String catalogJson = new String(((OtpErlangBinary) otp_request.elementAt(11)).binaryValue());
			return (Map<String, Object>) EmsUtil.fromJson(catalogJson, HashMap.class);
		} catch (Exception e) {
			throw new EmsValidationException(
					"Não foi possível obter o catálogo do request. Erro interno: " + e.getMessage());
		}
	}

	/**
	 * Obter o scopo oauth2 do request.
	 * 
	 * @return oauth2 scope
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getScope() {
		return scope;
	}

	/**
	 * Obter access_token do request.
	 * 
	 * @return oauth2 access token
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public String getAccessToken() {
		return access_token;
	}

	/**
	 * Obter o T1 do request.
	 * 
	 * @return long
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getT1() {
		return t1;
	}

	/**
	 * Obter o timeout do request.
	 * 
	 * @return int
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public long getTimeout() {
		return timeout;
	}

	/**
	 * Is POST ou PUT request
	 * 
	 * @return boolean
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public boolean isPostOrUpdateRequest() {
		return isPostOrUpdateRequestFlag;
	}

	/**
	 * Define o objeto payload do request.
	 * 
	 * @param object objeto a ser definido como payload
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public void setObject(Object object) {
		if (object != null) {
			this.payload = EmsUtil.toJson(object);
		} else {
			this.payload = null;
		}
	}

	/**
	 * Define um parâmetro do request.
	 * Nota: Este método não é suportado pois os parâmetros são extraídos da tupla
	 * Erlang.
	 * 
	 * @param nome  nome do parâmetro
	 * @param value valor do parâmetro
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public void setParam(String nome, String value) {
		throw new UnsupportedOperationException(
				"O método setParam não é suportado. Os parâmetros são extraídos automaticamente da tupla Erlang.");
	}

	/**
	 * Define uma querystring do request.
	 * Nota: Este método não é suportado pois as querystrings são extraídas da tupla
	 * Erlang.
	 * 
	 * @param nome  nome da querystring
	 * @param value valor da querystring
	 * @author Everton de Vargas Agilar
	 */
	@Override
	public void setQuery(String nome, String value) {
		throw new UnsupportedOperationException(
				"O método setQuery não é suportado. As querystrings são extraídas automaticamente da tupla Erlang.");
	}

}
