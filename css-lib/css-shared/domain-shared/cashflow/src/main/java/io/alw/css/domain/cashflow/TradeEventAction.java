package io.alw.css.domain.cashflow;

/**
 * The action taken on a trade event. The action of the first event version is always ADD.
 * From a settlement perspective, Authorize event action is not required. CSS only needs to know the event action(add,modify or remove) and the corresponding TradeEventType that was authorized.
 */
public enum TradeEventAction {
    ADD("Add"),
    MODIFY("Modify"),
    REMOVE("Remove");

    private final String value;

    TradeEventAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
