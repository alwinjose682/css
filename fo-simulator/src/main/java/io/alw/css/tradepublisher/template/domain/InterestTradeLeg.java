package io.alw.css.tradepublisher.template.domain;

import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.tradepublisher.template.model.InterestLegContext;

public final class InterestTradeLeg implements TradeDetail {
    private final InterestLegContext interestLegContext;
    private TradeLeg interestLeg;

    public InterestTradeLeg(InterestLegContext interestLegContext) {
        this.interestLegContext = interestLegContext;
    }

    public InterestLegContext interestLegContext() {
        return interestLegContext;
    }

    public TradeLeg interestLeg() {
        return interestLeg;
    }

    public void setInterestLeg(TradeLeg interestLeg) {
        this.interestLeg = interestLeg;
    }
}
