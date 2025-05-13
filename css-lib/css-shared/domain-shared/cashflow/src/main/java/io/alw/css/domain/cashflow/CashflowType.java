package io.alw.css.domain.cashflow;

public enum CashflowType {
    //A limited number of cashflow types
    SETTLEMENT("Settlement"),
    PREMIUM("Premium"),
    BROKERAGE("Brokerage"),
    INTEREST("Interest"),
    DIVIDEND("Dividend"), //Equity
    COUPON("Coupon"), //Bond
    COLLATERAL("Collateral"),
    LIQUIDITY("Liquidity"),
    ADJUSTMENT("Adjustment"),
    FEE("Fee"),
    OTHER("Other");

    private final String value;

    CashflowType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
