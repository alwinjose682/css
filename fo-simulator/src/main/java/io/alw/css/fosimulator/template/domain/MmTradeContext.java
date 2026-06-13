package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.RateType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLeg;

import java.util.List;

public final class MmTradeContext implements TradeMetadata {
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private final Trade trade;

    private int nextTradeLegId;
    private TradeLeg principalLeg;
    private TradeLeg maturityLeg;
    private final List<InterestTradeLeg> interestLegs;

    public MmTradeContext(RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis, Trade trade, List<InterestTradeLeg> interestLegs) {
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
        this.trade = trade;
        this.interestLegs = interestLegs;
    }


    @Override
    public TradeLeg rootTradeLeg() {
        return principalLeg;
    }

    @Override
    public void setRootTradeLeg(TradeLeg rootTradeLeg) {
        this.principalLeg = rootTradeLeg;
    }

    @Override
    public int nextTradeLegId() {
        return ++nextTradeLegId;
    }

    public TradeLeg principalLeg() {
        return principalLeg;
    }

    public TradeLeg maturityLeg() {
        return maturityLeg;
    }

    public void setMaturityLeg(TradeLeg maturityLeg) {
        this.maturityLeg = maturityLeg;
    }

    public void addInterestLeg(TradeLeg trdLeg) {
        if (trdLeg instanceof InterestTradeLeg intTrdLeg) {
            interestLegs.add(intTrdLeg);
        } else {
            throw new RuntimeException("An InterestTradeLeg is expected instead of TradeLeg");
        }
    }

    public List<InterestTradeLeg> interestLegs() {
        return interestLegs;
    }

    public RateType rateType() {
        return rateType;
    }

    public InterestPayoutFrequency ipFrequency() {
        return ipFrequency;
    }

    public InterestBasis interestBasis() {
        return interestBasis;
    }

    @Override
    public Trade trade() {
        return trade;
    }
}
