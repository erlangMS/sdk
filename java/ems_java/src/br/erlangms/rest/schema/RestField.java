/*
 * 
 */
package br.erlangms.rest.schema;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Classe que representa um campo de um objeto.
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 27/03/2019
 *
 */
public class RestField implements Serializable {

    private String voFieldName;
    private String fieldName;
    private RestFieldType fieldType;
    private Integer fieldLength;
    private boolean isAttributeObject;
    private boolean primaryKey;
    private RestFieldSortType sortType;
    private Field attrBase;
    private Field attr;
    private String attrBaseRelationName;

    public String getVoFieldName() {
        return voFieldName;
    }
    public void setVoFieldName(String voFieldName) {
        this.voFieldName = voFieldName;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public RestFieldType getFieldType() {
        return fieldType;
    }
    public void setFieldType(RestFieldType fieldType) {
        this.fieldType = fieldType;
    }
    public Integer getFieldLength() {
        return fieldLength;
    }
    public void setFieldLength(Integer length) {
        this.fieldLength = length;
    }

    public boolean isAttributeObject() {
        return this.isAttributeObject;
    }

    public void setIsAttributeObject(boolean isAttributeObject) {
        this.isAttributeObject = isAttributeObject;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.primaryKey = isPrimaryKey;
    }

    public void setSortType(RestFieldSortType sortType) {
        this.sortType = sortType;
    }

    public RestFieldSortType getSortType() {
        return sortType;
    }

    void setAttrBase(Field attrBase) {
        this.attrBase = attrBase;
    }

    public Field getAttrBase() {
        return attrBase;
    }

    public Field getAttr() {
        return attr;
    }

    public void setAttr(Field attr) {
        this.attr = attr;
    }

    public String getAttrBaseRelationName() {
        return attrBaseRelationName;
    }

    public void setAttrBaseRelationName(String attrBaseRelationName) {
        this.attrBaseRelationName = attrBaseRelationName;
    }

}
