/*
 * 
 */
package br.erlangms.rest.schema;

import java.util.List;
import java.util.Optional;

/**
 * Representa o esquema de dados para um provedor a qual
 * são composto por atributos que devem ser retornados em uma consulta REST.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 22/04/2019
 *
 */
public interface IRestApiSchema {
    public RestField addFieldAsInteger(final String voFieldName, final String fieldName);
    public RestField addFieldAsDouble(final String voFieldName, final String fieldName);
    public RestField addFieldAsDate(final String voFieldName, final String fieldName);
    public RestField addFieldAsTime(final String voFieldName, final String fieldName);
    public RestField addFieldAString(final String voFieldName, final String fieldName, Integer fieldLength);

    public RestField addFieldAsInteger(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsDouble(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsDate(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsTime(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAString(final String voFieldName, final String fieldName, Integer fieldLength, boolean filterRequired);

    public List<RestField> getFieldsList();
    public String getVoFields();
    public Optional<RestField> getFieldByVoName(final String voFieldName);

}
