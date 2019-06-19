package br.unb.erlangms.rest.filter.tokens;

import java.io.Serializable;

public class RestFilterToken implements Serializable {

    private final String value;
    private final int position;

    public RestFilterToken(String value, int position) {
        this.value = value;
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return value;
    }
}
