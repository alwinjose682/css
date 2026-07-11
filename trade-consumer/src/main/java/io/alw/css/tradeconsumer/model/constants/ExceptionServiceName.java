package io.alw.css.tradeconsumer.model.constants;

public enum ExceptionServiceName {
    TRADE("Trade"), CONFIRMATION("Confirmation");

    private final String value;

    ExceptionServiceName(String value) {
    this.value = value;
    }

    public String value(){
        return value;
    }
}
