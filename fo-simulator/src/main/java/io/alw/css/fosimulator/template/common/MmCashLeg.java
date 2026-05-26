package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.MmLeg;
import io.alw.css.domain.cashflow.MmType;
import io.alw.css.domain.cashflow.RateType;

public sealed class MmCashLeg implements MmMetadata permits InterestCashLeg {
    private final MmType mmType;
    private final MmLeg mmLeg;
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private FoCashMessage cashMessage;

    public MmCashLeg(MmType mmType, MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        this.mmType = mmType;
        this.mmLeg = mmLeg;
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
    }

    @Override
    public MmType mmType() {
        return mmType;
    }

    @Override
    public MmLeg mmLeg() {
        return mmLeg;
    }

    @Override
    public RateType rateType() {
        return rateType;
    }

    @Override
    public InterestPayoutFrequency ipFrequency() {
        return ipFrequency;
    }

    @Override
    public InterestBasis interestBasis() {
        return interestBasis;
    }

    public FoCashMessage cashMessage() {
        return cashMessage;
    }

    public void setCashMessage(FoCashMessage cashMessage) {
        this.cashMessage = cashMessage;
    }
}
