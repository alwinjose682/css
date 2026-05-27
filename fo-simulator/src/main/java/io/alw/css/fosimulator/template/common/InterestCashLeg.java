package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.MmLeg;
import io.alw.css.domain.cashflow.MmType;
import io.alw.css.domain.cashflow.RateType;

import java.math.BigDecimal;

public final class InterestCashLeg extends MmCashLeg {
    private InterestLegContext interestLegContext;

    public InterestCashLeg(MmType mmType, MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        super(mmType, mmLeg, rateType, ipFrequency, interestBasis);
    }

    public InterestLegContext interestLegContext() {
        return interestLegContext;
    }

    public void setInterestLegContext(InterestLegContext interestLegContext) {
        this.interestLegContext = interestLegContext;
    }
}
