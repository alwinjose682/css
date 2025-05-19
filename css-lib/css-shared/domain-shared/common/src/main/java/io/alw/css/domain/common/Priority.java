package io.alw.css.domain.common;

public enum Priority {
    NORMAL("Normal"),
    URGENT("Urgent");

    private final String value;

    Priority(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
