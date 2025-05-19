package io.alw.css.domain.common;

public enum PrimSec {
    P("P"),
    S("S");

    private final String value;

    PrimSec(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
