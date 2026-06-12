package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.common.RateType;
import io.alw.css.domain.trade.TradeLegType;

public sealed class MmCashLeg implements CashLeg, MmMetadata permits InterestCashLeg {
    private final TradeLegType mmLegType;
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private FoCashMessage cashMessage;
    private TradeContext trdCtx;

    public MmCashLeg(TradeLegType mmLegType, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        this.mmLegType = mmLegType;
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
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
    public TradeLegType cashLegType() {
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
