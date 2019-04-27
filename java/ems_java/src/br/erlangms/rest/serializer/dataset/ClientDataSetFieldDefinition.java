/*
 * 
 */
package br.erlangms.rest.serializer.dataset;

import br.erlangms.rest.schema.RestField;

/**
 *
 * @author Jader Adiel Schmitt
 *
 * Representa uma definiÃ§Ã£o de campo de um ClientDataSet do delphi
 */
public class ClientDataSetFieldDefinition {

    private String name;
    private ClientDataSetDataType dataType;
    private Integer size;
    private Boolean required;

    public ClientDataSetFieldDefinition(){

    }

    public ClientDataSetFieldDefinition(final RestField restField){
        final ClientDataSetDataType clientDataSetDataType = ClientDataSetDataType.fromRestApiType(restField.getFieldType());
        this.name = restField.getVoFieldName();
        this.dataType = clientDataSetDataType;
        this.size = restField.getFieldLength();
        if (clientDataSetDataType.getDefautSize() > 0 ){
            if (this.size == null || this.size <= 0){
                this.size = clientDataSetDataType.getDefautSize();
            }
        }
    }

    public ClientDataSetFieldDefinition(final String name, final ClientDataSetDataType dataType){
        this(name, dataType, null);
    }

    public ClientDataSetFieldDefinition(final String name, final ClientDataSetDataType dataType, Integer size){
        this(name, dataType, size, null);
    }

    public ClientDataSetFieldDefinition(final String name, final ClientDataSetDataType dataType, final Integer size, final Boolean required){
        this.name = name;
        this.dataType = dataType;
        this.size = size;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientDataSetDataType getDataType() {
        return dataType;
    }

    public void setDataType(ClientDataSetDataType dataType) {
        this.dataType = dataType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

}
