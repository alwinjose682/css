package io.alw.css.domain.common;

/// Note: Each `Counterparty` has a netting profile that specifies which [CssNettingType] is to be used for each TradeType/Product.
/// The Counterparty netting profile can have other optional criteria as well, like netByParentCounterpartyCode etc.
public enum CssNettingType {
    GROSS("Gross"),
    WITHIN_PRODUCT("WithinProduct"),
    ACROSS_PRODUCT("AcrossProduct");

    private final String value;

    CssNettingType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
