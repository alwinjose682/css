package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.MmTradeType;
import io.alw.css.domain.cashflow.RateType;
import io.alw.css.fosimulator.template.model.InterestLegContext;

public final class InterestCashLeg extends MmCashLeg {
    private InterestLegContext interestLegContext;

    public InterestCashLeg(MmTradeType mmTradeType, CashLegType mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        super(mmTradeType, mmLeg, rateType, ipFrequency, interestBasis);
    }

    public InterestLegContext interestLegContext() {
        return interestLegContext;
    }

    public void setInterestLegContext(InterestLegContext interestLegContext) {
        this.interestLegContext = interestLegContext;
    }
}
