package io.alw.css.domain.cashflow;

public enum PayOrRecieve {
    PAY("Pay"),
    RECEIVE("Recieve");

    private final String value;

    PayOrRecieve(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
