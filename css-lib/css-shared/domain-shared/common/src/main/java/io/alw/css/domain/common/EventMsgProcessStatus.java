package io.alw.css.domain.common;

public enum EventMsgProcessStatus {
    SENT("SENT"),
    RECEIVED("RECEIVED"),
    PROCESSED("PROCESSED"),
    REJECTED("REJECTED");

    private final String value;

    EventMsgProcessStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
