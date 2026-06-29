package io.alw.css.tradepublisher.template.model;

public enum AmendableFieldType {
    VALUE_DATE(AmendmentTarget.TRADE_LEG),
    AMOUNT(AmendmentTarget.TRADE_LEG),
    COUNTERPARTY_CODE(AmendmentTarget.TRADE_LEG);

    private final AmendmentTarget amendmentTarget;

    AmendableFieldType(AmendmentTarget amendmentTarget) {
        this.amendmentTarget = amendmentTarget;
    }

    public AmendmentTarget amendmentTarget() {
        return amendmentTarget;
    }
}
