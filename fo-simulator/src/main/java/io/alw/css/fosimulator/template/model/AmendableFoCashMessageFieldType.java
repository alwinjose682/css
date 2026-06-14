package io.alw.css.fosimulator.template.model;

public enum AmendableFoCashMessageFieldType {
    VALUE_DATE(AmendmentTarget.TRADE_LEG),
    AMOUNT(AmendmentTarget.TRADE_LEG),
    COUNTERPARTY_CODE(AmendmentTarget.TRADE_LEG);

    private final AmendmentTarget amendmentTarget;

    AmendableFoCashMessageFieldType(AmendmentTarget amendmentTarget) {
        this.amendmentTarget = amendmentTarget;
    }

    public AmendmentTarget amendmentTarget() {
        return amendmentTarget;
    }
}
