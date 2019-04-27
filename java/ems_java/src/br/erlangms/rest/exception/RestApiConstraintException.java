/*
 * 
 */
package br.erlangms.rest.exception;

/**
 * Exceptions que violam as restrições da API RESTful
 *
 * @author evertonagilar
 */
public class RestApiConstraintException extends RestApiException {
	private static final long serialVersionUID = -5250700481655729189L;
	public final static String REQUISICAO_SO_PODE_SER_ALTERADA_SE_INVALIDADA = "A requisição só pode ser alterada se o seu estado for OPEN.";
    public final static String OPERADOR_LIMIT_FORA_INTERVALO_PERMITIDO = "Operador limit inválido. Motivo: fora do intervalo permitido.";
    public final static String OPERADOR_OFFSET_FORA_INTERVALO_PERMITIDO = "Operador offset inválido. Motivo: fora do intervalo permitido.";
    public final static String OPERADOR_ID_FORA_INTERVALO_PERMITIDO = "Operador id inválido. Motivo: fora do intervalo permitido.";
    public final static String OPERADOR_ID_NAO_EH_INTEIRO_MAIOR_QUE_ZERO = "Operador id inválido. Motivo: não Ã© um valor inteiro maior que zero.";
    public final static String OPERADOR_FORMAT_INVALIDO = "Operador format inválido. Pode ser raw, vo, entity ou dataset.";
    public final static String REQUISICAO_PRECISA_PROVIDER_PARA_PARSE = "A requisição precisa de uma instancia IRestApiProvider para ser validada.";
    public final static String OPERADOR_DE_ATRIBUTO_INVALIDO = "Operador de atributo %s inválido.";
    public final static String OPERADOR_SORT_OBRIGATORIO = "O webservice invocado precisa do operador sort.";
    public final static String OPERADOR_FIELDS_OBRIGATORIO = "O webservice invocado precisa do operador fields.";
    public final static String OPERADOR_OFFSET_OBRIGATORIO = "O webservice invocado precisa do operador offset.";
    public final static String OPERADOR_LIMIT_OBRIGATORIO = "O webservice invocado precisa do operador limit.";
    public final static String OPERADOR_FILTER_OBRIGATORIO = "O webservice invocado precisa do operador filter.";
    public final static String OPERADOR_ID_OBRIGATORIO = "O webservice invocado precisa do operador id.";
    public final static String OPERADOR_FORMAT_OBRIGATORIO = "O webservice invocado precisa do operador format.";
    public final static String REQUEST_OBRIGATORIO_PARA_CRIAR_QUERY = "Para criar uma query informe um request válido.";
    public final static String REQUEST_OBRIGATORIO_PARA_PROVIDER = "Obrigatório definir um objeto RestApiConstraints para o provider.";
    public final static String ATRIBUTO_NAO_EXISTE = "Atributo %s não existe.";
    public final static String ATRIBUTO_IN_OPERADOR_SORT_NAO_EXISTE = "Atributo %s declarado no operador sort não existe.";
    public final static String ATRIBUTO_IN_OPERADOR_FILTER_NAO_EXISTE = "Atributo %s declarado no operador filter não existe.";
    public final static String ATRIBUTO_IN_OPERADOR_FIELDS_NAO_EXISTE = "Atributo %s declarado no operador fields não existe.";
    public final static String ATRIBUTO_IN_ENTITY_NAO_EXISTE = "Atributo %s declarado no schema não existe na entidade.";
    public final static String ERRO_CONVERTER_ENTIDADE_EM_VO = "Não foi possÃ­vel converter entidade para vo. Erro interno: ";
    public final static String REQUEST_OBRIGATORIO = "Necessário informar uma requisição válida.";
    public final static String REQUEST_COM_ID_OBRIGATORIO = "Necessário informar uma requisição com id válida.";
    public final static String ERRO_EXECUTA_FIND = "Falha na execução da operação find. Motivo: %s";
    public final static String FORMATO_ENTITY_NAO_SUPORTA_OPERADOR_FIELS = "Não é permitido informar o operador fields quando o operador format é entity";
    public final static String METAQUERY_PROVIDER_NAO_SUPORTA_FORMATO_ENTITY = "Provider de metaquery não suporta o formato entity";
    public final static String OPERATOR_FILTER_INVALID = "Expressão json %s não permitida, informe ao menos uma condição.";
    public final static String PROVIDER_FACTORY_FAILED = "Não foi possÃ­vel instanciar o provider";
    public final static String RESULT_CACHE_PERMITIDO_SE_REQUEST_CACHE_ATIVO = "O result-cache só é permitido quando request-cache está ativo.";
    public final static String NECESSARIO_INFORMAR_PROVIDER = "Obrigatório informaro o provider.";
    public final static String OPERADOR_FLAGS_INVALIDO = "Operador flags inválido.";
    public final static String ERRO_OBTER_DADOS_QUERY = "Errp ao obter dados da query.";

    public RestApiConstraintException(String message) {
        super(message);
    }

}
