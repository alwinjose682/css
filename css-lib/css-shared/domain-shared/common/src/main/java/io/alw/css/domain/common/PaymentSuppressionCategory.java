package io.alw.css.domain.common;

/// PaymentSuppressionCategory can be used by both Cashflow and Payment records. This may be set for different versions of the CF by different CSS services
public enum PaymentSuppressionCategory {
    // Below are applicable for both Cashflow and Payment
    AMOUNT_TOO_SMALL("AmountTooSmall"),
    MISSED_CURRENCY_CUTOFF("MissedCurrencyCutOff"),
    INACTIVE_OBLIGATION_DATA("InactiveObligationData"), // One of the ObligationData is inactive(currCode, entityCode, CounterpartyCode)
    INACTIVE_BOOK("InactiveBook"),

    // Below are applicable only for Cashflow
    INTERBOOK("InterbookCashflow"),
    BACK_VALUED_CASHFLOW("BackValuedCashflow"), // All back valued cashflows are simply suppressed

    // Below is applicable only for Payment
    INACTIVE_NOSTRO("InactiveNostro"),
    INACTIVE_CLIENT_SSI("InactiveClientSSI");

    private final String value;

    PaymentSuppressionCategory(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
