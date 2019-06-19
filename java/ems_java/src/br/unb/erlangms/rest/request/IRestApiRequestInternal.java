package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.contract.RestApiDataFormat;
import br.unb.erlangms.rest.contract.RestApiVerb;
import br.unb.erlangms.rest.filter.RestFilterCondition;
import br.unb.erlangms.rest.filter.ast.RestFilterAST;
import br.unb.erlangms.rest.provider.IRestApiProvider;
import br.unb.erlangms.rest.schema.RestField;
import java.util.List;
import java.util.Optional;

/**
 * Interface responsável por representar uma requisição internamente no parser.
 *
 * Os operadores da API são:
 * fields 		define os campos que devem ser retornados da consulta. Se for null, vai usar a lista de campos
 * 				definido pelo provider que por default é retornar todos os atributos do objeto.
 * filter 		define um filtro para a consulta. Se for null, retorna todos os objetos do provider utilizado.
 * sort 		define os atributos para ordenação. Se for null, vai usar a ordenação definida pelo provider.
 * limit 		define o limite de objetos retornados na consulta. Se for null, vai usar o limite definido pelo provider.
 * offset 		define o offset para paginação de registros na consulta. Se for null, vai iniciar em 0.
 * id 			define que deve buscar um objeto específico do conjunto de objetos retornados pelo provider.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public interface IRestApiRequestInternal {
    public int getRID();                        // Request ID
    public IRestApiRequest getRequestUser();    // Request do usuário
    public String getFields();
    public String getFilter();
    public Long getId();
    public Integer getLimit();
    public Long getMaxId();
    public Integer getMaxLimit();
    public Integer getOffset();
    public String getSort();
    public RestApiDataFormat getApiDataFormat();
    public IRestApiProvider getApiProvider();
    public List<RestField> getFieldsList();
    public RestFilterAST getFilterAST();
    public List<RestField> getSortList();
    public RestApiVerb getApiVerb();
    public void parse();
    public void open();
    public boolean isParsed();
    public RestApiRequestState getState();
    public boolean isDefault();
    public IRestApiRequestFlags getFlags();
    public Optional<RestFilterCondition> findConditionByVoFieldName(final RestField field);
}
