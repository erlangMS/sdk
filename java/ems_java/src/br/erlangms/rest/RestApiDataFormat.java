/*
 * 
 */
package br.erlangms.rest;

import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.util.RestUtils;

/**
 * Lista os formatos de dado suportados pela REST API
 *
 *    raw     dado bruto
 *    entity  lista de entidades JPA
 *    vo      array de maps
 *    json    formato json
 *    dataset formato xml que representa a propriedade data do Dataset do Delphi
 * 
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 05/04/2019
 *
 */
public enum RestApiDataFormat {
    RAW,
    VO,
    ENTITY,
    JSON,
    DATASET;

    public static String enumToStr(RestApiDataFormat dataFormat) {
        switch (dataFormat) {
            case RAW:
                return "raw";
            case ENTITY:
                return "entity";
            case DATASET:
                return "dataset";
            case JSON:
                return "json";
            default:
                return "vo";
        }
    }
    public static RestApiDataFormat strToEnum(String dataFormatStr) {
        dataFormatStr = RestUtils.removeAllSpaces(dataFormatStr);
        dataFormatStr = RestUtils.unquoteString(dataFormatStr);
        switch (dataFormatStr) {
            case "raw":
                return RAW;
            case "vo":
                return VO;
            case "json":
                return JSON;
            case "entity":
                return ENTITY;
            case "dataset":
                return DATASET;
            default:
                throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_FORMAT_INVALIDO);
        }
    }
}
