package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.RateType;
import io.alw.css.fosimulator.template.model.InterestLegContext;

public final class InterestCashLeg extends MmCashLeg {
    private InterestLegContext interestLegContext;

    public InterestCashLeg(CashLegType mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        super(mmLeg, rateType, ipFrequency, interestBasis);
    }

    public InterestLegContext interestLegContext() {
        return interestLegContext;
    }

    public void setInterestLegContext(InterestLegContext interestLegContext) {
        this.interestLegContext = interestLegContext;
    }
}
