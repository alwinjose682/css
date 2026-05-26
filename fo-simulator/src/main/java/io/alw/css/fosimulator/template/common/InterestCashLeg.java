package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.MmLeg;
import io.alw.css.domain.cashflow.MmType;
import io.alw.css.domain.cashflow.RateType;

import java.math.BigDecimal;

public final class InterestCashLeg extends MmCashLeg {
    private BigDecimal lastInterestLegAmount;

    public InterestCashLeg(MmType mmType, MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        super(mmType, mmLeg, rateType, ipFrequency, interestBasis);
    }

    public BigDecimal lastInterestLegAmount() {
        return lastInterestLegAmount;
    }

    public void setLastInterestLegAmount(BigDecimal lastInterestLegAmount) {
        this.lastInterestLegAmount = lastInterestLegAmount;
    }
}
