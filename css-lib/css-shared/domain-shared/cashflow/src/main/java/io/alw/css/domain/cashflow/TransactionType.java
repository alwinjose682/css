package io.alw.css.domain.cashflow;

public enum TransactionType {
    CLIENT("Client"), // Trade booked for a client
    MARKET("Market"), // Trade booked on behalf of the bank with the market
    INTER_BOOK("InterBook"),
    INTER_BRANCH("InterBranch"),
    INTER_COMPANY("InterCompany"),
    CORPORATE_ACTION("CorporateAction"); // Transaction triggered by CorporateAction

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    private String value() {
        return value;
    }
}
