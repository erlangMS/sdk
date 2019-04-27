/*
 * 
 */
package br.erlangms.rest.request;

import br.erlangms.rest.RestApiDataFormat;
import br.erlangms.rest.filter.ast.RestFilterAST;
import br.erlangms.rest.provider.IRestApiProvider;
import br.erlangms.rest.schema.RestField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface responsável por representar uma requisição RESTful de maneira agnóstica
 *
 * <p>
 * As requisições encapsulam os parâmetros aceitos e permitem validar a requisição
 * antes de realizar a operação na fonte de dados.</p>
 *
 * <p>
 * Os operadores da API são:</p>
 * <ul>
 * <li>fields - define os campos que devem ser retornados da consulta. Se for null, vai usar a lista de campos
 * definido pelo provider que por default Ã© retornar todos os atributos do objeto.</li>
 * <li>filter - define um filtro para a consulta. Se for null, retorna todos os objetos do provider utilizado.</li>
 * <li>sort - define os atributos para ordenação. Se for null, vai usar a ordenação definida pelo provider.</li>
 * <li>limit - define o limite de objetos retornados na consulta. Se for null, vai usar o limite definido pelo provider.</li>
 * <li>offset - define o offset para paginação de registros na consulta. Se for null, vai iniciar em 0.</li>
 * <li>id - define que deve buscar um objeto específico do conjunto de objetos retornados pelo provider.</li>
 * </ul>
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 21/03/2019
 *
 */
public interface IRestApiRequest extends Serializable {
    public IRestApiProvider getApiProvider();
    public void setApiProvider(final IRestApiProvider apiProvider);
    public String getFields();
    public List<RestField> getFieldsList();
    public String getFilter();
    public RestFilterAST getFilterAST();
    public Long getId();
    public Integer getLimit();
    public Long getMaxId();
    public Integer getMaxLimit();
    public Integer getOffset();
    public String getSort();
    public List<RestField> getSortList();
    public RestApiDataFormat getDataFormat();
    public void setFields(final String fields);
    public void setFilter(String filter);
    public void setId(final Long id);
    public void setIdAsString(String idStr);
    public void setLimit(Integer limit);
    public void setMaxId(final Long maxId);
    public void setMaxLimit(Integer maxLimit);
    public void setOffset(Integer offset);
    public void setDataFormat(RestApiDataFormat dataFormat);
    public void setSort(final String sort);
    public IRestApiRequestFlags getFlags();
    public void setFlagsAsString(String flags);
    public void parse();
    public void open();
    public boolean isParsed();
    public RestApiRequestState getState();
    public boolean isDefault();
    public void setDefaultsIfEmpty();
    public IRestApiRequestStatistics getStatistics();
    public void __setFields(String join);
    public void __setFieldsList(ArrayList<RestField> arrayList);
}
