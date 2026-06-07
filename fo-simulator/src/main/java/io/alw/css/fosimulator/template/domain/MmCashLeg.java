package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.MmTradeType;
import io.alw.css.domain.cashflow.RateType;

public sealed class MmCashLeg implements CashLeg, MmMetadata permits InterestCashLeg {
    private final MmTradeType mmTradeType;
    private final CashLegType mmLegType;
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private FoCashMessage cashMessage;
    private TradeContext trdCtx;

    public MmCashLeg(MmTradeType mmTradeType, CashLegType mmLegType, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        this.mmTradeType = mmTradeType;
        this.mmLegType = mmLegType;
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
    }

    @Override
    public MmTradeType mmType() {
        return mmTradeType;
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

    @Override
    public CashLegType cashLegType() {
        return mmLegType;
    }

    @Override
    public FoCashMessage cashMessage() {
        return cashMessage;
    }

    @Override
    public void setCashMessage(FoCashMessage cashMessage) {
        this.cashMessage = cashMessage;
    }

    @Override
    public TradeContext tradeContext() {
        return trdCtx;
    }

    @Override
    public void setTradeContext(TradeContext trdCtx) {
        if (this.trdCtx != null) {
            throw new RuntimeException("Reference to TradeContext can be set only once");
        }
        this.trdCtx = trdCtx;
    }
}
