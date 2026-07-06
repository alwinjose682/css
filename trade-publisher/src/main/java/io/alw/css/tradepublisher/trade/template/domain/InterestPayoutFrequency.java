package io.alw.css.tradepublisher.trade.template.domain;

public enum InterestPayoutFrequency {
    DAY(1),
    MONTHLY(30),
    QUARTERLY(90),
    SEMI_ANNUALLY(180),
    YEARLY(360),
    PRINCIPAL_MATURITY(0);

    private final int offsetDays;

    InterestPayoutFrequency(int offsetDays) {
        this.offsetDays = offsetDays;
    }

    public int offsetDays() {
        return offsetDays;
    }
}
