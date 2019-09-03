package br.unb.erlangms.rest.exception;

import javax.ejb.ApplicationException;

/**
 * Exceptions que violam as restrições da API RESTful
 *
 * @author evertonagilar
 */
@ApplicationException(rollback = true)
public class RestApiException extends RuntimeException {
    private static final long serialVersionUID = -5121898496866723234L;
    public final static String REQUISICAO_SO_PODE_SER_ALTERADA_SE_INVALIDADA = "A requisição só pode ser alterada se o seu estado for OPEN.";
    public final static String OPERADOR_LIMIT_FORA_INTERVALO_PERMITIDO = "Operador limit inválido. Motivo: fora do intervalo permitido.";
    public final static String OPERADOR_OFFSET_FORA_INTERVALO_PERMITIDO = "Operador offset inválido. Motivo: fora do intervalo permitido.";
    public final static String OPERADOR_ID_FORA_INTERVALO_PERMITIDO = "Operador id inválido. Motivo: fora do intervalo permitido.";
    public final static String OPERADOR_ID_NAO_EH_INTEIRO_MAIOR_QUE_ZERO = "Operador id inválido. Motivo: não é um valor inteiro maior que zero.";
    public final static String OPERADOR_FORMAT_INVALIDO = "Operador format inválido. Pode ser raw, vo, entity ou dataset.";
    public final static String REQUISICAO_PRECISA_PROVIDER_PARA_PARSE = "A requisição precisa de uma instância IRestApiProvider para ser validada.";
    public final static String OPERADOR_DE_ATRIBUTO_INVALIDO = "Operador de atributo %s inválido.";
    public final static String OPERADOR_SORT_OBRIGATORIO = "O webservice invocado precisa do operador sort.";
    public final static String OPERADOR_FIELDS_OBRIGATORIO = "O webservice invocado precisa do operador fields.";
    public final static String OPERADOR_OFFSET_OBRIGATORIO = "O webservice invocado precisa do operador offset.";
    public final static String OPERADOR_LIMIT_OBRIGATORIO = "O webservice invocado precisa do operador limit.";
    public final static String OPERADOR_FILTER_OBRIGATORIO = "O webservice invocado precisa do operador filter.";
    public final static String OPERADOR_ID_OBRIGATORIO = "O webservice invocado precisa do operador id.";
    public final static String OPERADOR_FORMAT_OBRIGATORIO = "O webservice invocado precisa do operador format.";
    public final static String REQUEST_OBRIGATORIO_PARA_CRIAR_QUERY = "Para criar uma query informe um request válido.";
    public final static String REQUEST_OBRIGATORIO_PARA_PROVIDER = "É obrigatório definir um objeto RestApiConstraints para o provider.";
    public final static String ATRIBUTO_INVALIDO_SCHEMA = "Atributo %s no schema do provider %s não é um nome válido.";
    public final static String NOME_ATRIBUTO_VO_OBRIGATORIO_SCHEMA = "Nome do atributo é obrigatório.";
    public final static String ATRIBUTO_IN_OPERADOR_SORT_NAO_EXISTE = "Atributo %s declarado no operador sort não existe.";
    public final static String ATRIBUTO_IN_OPERADOR_FILTER_NAO_EXISTE = "Atributo %s declarado no operador filter não existe.";
    public final static String ATRIBUTO_IN_OPERADOR_FIELDS_NAO_EXISTE = "Atributo %s declarado no operador fields não existe.";
    public final static String ATRIBUTO_IN_ENTITY_NAO_EXISTE = "Atributo %s declarado no schema não existe na entidade.";
    public final static String ATRIBUTO_DUPLICADO_SCHEMA = "Atributo %s no schema do provider %s foi declarado mais de uma vez.";
    public final static String ERRO_CONVERTER_ENTIDADE_EM_VO = "Não foi possível converter entidade para vo. Erro interno: ";
    public final static String REQUEST_E_PROVIDER_OBRIGATORIO = "É necessário informar uma requisição e um provider válido.";
    public final static String REQUEST_COM_ID_OBRIGATORIO = "É necessário informar uma requisição com id válida.";
    public final static String ERRO_EXECUTA_FIND = "Falha desconhecida na execução da operação find";
    public final static String FORMATO_ENTITY_NAO_SUPORTA_OPERADOR_FIELS = "Não é permitido informar o operador fields quando o operador format é entity";
    public final static String METAQUERY_PROVIDER_NAO_SUPORTA_FORMATO_ENTITY = "Provider de metaquery não suporta o formato entity";
    public final static String OPERATOR_FILTER_INVALID = "Expressão json %s não permitida, informe ao menos uma condição.";
    public final static String PROVIDER_FACTORY_FAILED = "Não foi possível instânciar o provider";
    public final static String RESULT_CACHE_PERMITIDO_SE_REQUEST_CACHE_ATIVO = "O result-cache só é permitido quando request-cache está ativo.";
    public final static String NECESSARIO_INFORMAR_PROVIDER = "É obrigatório informaro o provider.";
    public final static String OPERADOR_FLAGS_INVALIDO = "Operador flags inválido.";
    public final static String ERRO_OBTER_DADOS_QUERY = "Erro ao obter dados da consulta.";
    public final static String VALOR_ATRIBUTO_INCOMPATIVEL = "Valor da condição incompatível com o atributo %s.";
    public final static String VALOR_ATRIBUTO_INTEGER_INVALIDO = "Valor %s fornecido ao atributo %s inválido pois é esperado um número inteiro.";
    public final static String VALOR_ATRIBUTO_DOUBLE_INVALIDO = "Valor %s fornecido ao atributo %s inválido pois é esperado um número.";
    public final static String VALOR_ATRIBUTO_LONG_INVALIDO = "Valor %s fornecido ao atributo %s inválido pois é esperado um número.";
    public final static String VALOR_ATRIBUTO_DATE_INVALIDO = "Valor %s fornecido ao atributo %s inválido pois é esperado uma data no formato %s.";
    public final static String VALOR_MAX_ATRIBUTO_FORA_INTERVALO = "Valor %s fornecido ao atributo %s fora do intervalo máximo permitido. Máximo: %s.";
    public final static String VALOR_MIN_ATRIBUTO_FORA_INTERVALO = "Valor %s fornecido ao atributo %s fora do intervalo mínimo permitido. Mínimo: %s.";
    public final static String MIN_LENGTH_GT_FIELD_LENGTH_SCHEMA = "Largura mínima do atributo % maior que a largura definida no schema.";
    public final static String INVALID_MIN_LENGTH_ATRIBUTO = "Valor fornecido ao atributo %s deve ter pelo menos %s caracteres.";
    public final static String INVALID_MIN_LENGTH_SCHEMA = "Parâmetro minLength definido no atributo % inválido. Informe um valor entre 1 e 9999.";
    public final static String INVALID_FIELD_LENGTH_SCHEMA = "Parâmetro fieldLength definido no atributo % inválido. Informe um valor entre 1 e 9999.";
    public final static String TYPE_REST_FIELD_EXPRESSION_INVALID = "Tipo de dados retornado na expressão não suportado.";
    public final static String ATRIBUTO_EXPRESSION_NAO_DEVE_APARECER_OPERADOR_FILTER = "Atributo %s não deve aparecer no operador filter para o ws solicitado.";
    public final static String ATRIBUTO_EXPRESSION_NAO_DEVE_APARECER_OPERADOR_SORT = "Atributo %s não deve aparecer no operador sort para o ws solicitado.";
    public final static String ATRIBUTO_OBRIGATORIO_NO_FILTRO = "Atributo %s é obrigatório no operador filter.";
    public final static String INVALID_PAYLOAD_JSON = "O payload da requisição não é um json válido.";
    public final static String OBJETO_NAO_EXISTE = "Objeto com id %s não existe.";
    public final static String ERRO_PUT_SERVICE = "Erro desconhecido ao executar chamada PUT.";
    public final static String ERRO_POST_SERVICE = "Erro desconhecido ao executar chamada POST.";
    public final static String ERRO_GET_SERVICE = "Erro desconhecido ao executar chamada GET.";
    public final static String DATA_FORMAT_ENTITY_REQUER_ENTITY_CLASS = "Não é permitido informar entity para o operador format quando o provider do ws não define um entityClass.";
    public final static String ERRO_PERSISTIR_DADOS = "Não foi possível persistir os dados.";
    public final static String WS_NAO_SUPORTA_VERBO = "Verbo não suportado neste web service.";
    public final static String ACCESS_DENIED = "Acesso negado.";
    public final static String GET_ENTITYMANAGER_OBRIGATORIO = "A classe RestApiManager precisa que seja implementada o método getEntityManager().";

    public RestApiException(final String message) {
        super(message);
    }

    public RestApiException(final String message, final Object... args) {
        super(String.format(message, args));
    }

}
