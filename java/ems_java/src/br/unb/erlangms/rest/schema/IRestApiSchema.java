package br.unb.erlangms.rest.schema;

import br.unb.erlangms.rest.contract.IRestApiContract;
import java.util.List;
import java.util.Optional;

/**
 * Representa o esquema de dados para um provedor a qual
 * é composto por atributos que devem ser retornados em uma consulta REST.
 *
 * @author Everton de Vargas Agilar 
 * @version 1.0.0
 * @since 22/04/2019
 *
 */
public interface IRestApiSchema {
    public RestField addFieldAsIdentify(final String voFieldName, final String fieldName);
    public RestField addFieldAsInteger(final String voFieldName, final String fieldName);
    public RestField addFieldAsLong(final String voFieldName, final String fieldName);
    public RestField addFieldAsNonNegInteger(final String voFieldName, final String fieldName);
    public RestField addFieldAsDouble(final String voFieldName, final String fieldName);
    public RestField addFieldAsDate(final String voFieldName, final String fieldName);
    public RestField addFieldAsTime(final String voFieldName, final String fieldName);
    public RestField addFieldAsDay(final String voFieldName, final String fieldName);
    public RestField addFieldAsMonth(final String voFieldName, final String fieldName);
    public RestField addFieldAsYear(final String voFieldName, final String fieldName);
    public RestField addFieldAsEMail(final String voFieldName, final String fieldName);

    public RestField addFieldAsExpression(final String voFieldName, final RestFieldExpressionCallback callback);

    public RestField addFieldAsString(final String voFieldName, final String fieldName);
    public RestField addFieldAsString(final String voFieldName, final String fieldName, Integer fieldLength);
    public RestField addFieldAsString(final String voFieldName, final String fieldName, Integer fieldLength, boolean filterRequired, boolean autoTrim);

    public RestField addFieldAsUpperCaseString(final String voFieldName, final String fieldName);
    public RestField addFieldAsUpperCaseString(final String voFieldName, final String fieldName, Integer fieldLength);
    public RestField addFieldAsUpperCaseString(final String voFieldName, final String fieldName, Integer fieldLength, boolean filterRequired, boolean autoTrim);

    public RestField addFieldAsLowerCaseString(final String voFieldName, final String fieldName);
    public RestField addFieldAsLowerCaseString(final String voFieldName, final String fieldName, Integer fieldLength);
    public RestField addFieldAsLowerCaseString(final String voFieldName, final String fieldName, Integer fieldLength, boolean filterRequired, boolean autoTrim);

    public RestField addFieldAsIdentify(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsInteger(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsLong(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsNonNegInteger(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsDouble(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsDate(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsTime(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsDay(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsMonth(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsYear(final String voFieldName, final String fieldName, boolean filterRequired);
    public RestField addFieldAsEmail(final String voFieldName, final String fieldName, boolean filterRequired);




    public List<RestField> getFieldsList();
    public List<RestField> getRequiredFieldsList();
    public String getVoFields();
    public Optional<RestField> getFieldByVoName(final String voFieldName);
    public IRestApiContract getContract();



}
