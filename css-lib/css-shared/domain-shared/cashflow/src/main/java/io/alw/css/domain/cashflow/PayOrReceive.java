package io.alw.css.domain.cashflow;

public enum PayOrReceive {
    PAY("Pay"),
    RECEIVE("Recieve");

    private final String value;

    PayOrReceive(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
