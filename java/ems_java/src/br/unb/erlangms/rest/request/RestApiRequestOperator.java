package br.unb.erlangms.rest.request;

import java.io.Serializable;

/**
 *
 * @author evertonagilar
 */
public enum RestApiRequestOperator implements Serializable {
    Filter,
    Fields,
    Sort,
    Limit,
    Offset,
    Id,
    Format;

    public static String enumToOperatorToken(final RestApiRequestOperator operator) {
        switch (operator) {
            case Filter:
                return "filter";
            case Fields:
                return "fields";
            case Sort:
                return "sort";
            case Limit:
                return "limit";
            case Offset:
                return "offset";
            case Format:
                return "format";
            default:
                return "id";
        }
    }
}
