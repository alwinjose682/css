package io.alw.css.domain.common;

// In CSS, Cashflow records can be created only by CSS services and manually by operations users
public enum InputBy {
    CSS_SYS("CSS_SYS"), MAN("MAN");

    private final String value;

    InputBy(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
