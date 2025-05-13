package io.alw.css.domain.cashflow;

/**
 * Limited number of link types
 * Example: An FX trade may be executed because of CorporateAction, settlement of an equity trade(OrderNumber, ISIN) or Bond/Fixed Income or a payment due to a fee, charges etc.
 */
public enum TradeLinkType {
    CORPORATE_ACTION("CorporateAction"),
    ORDER_NUMBER("OrderNumber"), // The exchange order number
    STRUCTURED_TRADE("StructuredTrade"),
    TWO_WAY_SWITCH("TwoWaySwitch"), // Internal trade between two books or branch or company
    NDF("NDFOriginal");

    private final String value;

    TradeLinkType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
