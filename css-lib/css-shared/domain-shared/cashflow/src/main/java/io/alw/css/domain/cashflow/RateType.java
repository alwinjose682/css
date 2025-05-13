package io.alw.css.domain.cashflow;

public enum RateType {
    FIXED("Fixed"),
    FLOAT("Float");

    private final String value;

    RateType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
