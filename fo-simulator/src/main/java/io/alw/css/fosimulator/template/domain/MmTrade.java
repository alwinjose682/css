package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.RateType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;

import java.util.ArrayList;
import java.util.List;

public final class MmTrade implements ExtendedTrade {
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private Trade trade;

    private int nextTradeLegId;
    private TradeLeg principalLeg;
    private TradeLeg maturityLeg;
    private final List<InterestTradeLeg> interestLegs;

    public MmTrade(RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;
        this.interestLegs = new ArrayList<>();
        this.nextTradeLegId = resetTradeLegIdProvider();
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

    @Override
    public int resetTradeLegIdProvider() {
        nextTradeLegId = 0;
        return nextTradeLegId;
    }

    @Override
    public void setTrade(Trade trade) {
        this.trade = trade;
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

    public void addInterestLeg(TradeDetail trdLeg) {
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

    @Override
    public Iterable<TradeDetail> allTradeLegs() {
        var allTradeLegs = new ArrayList<TradeDetail>();
        allTradeLegs.add(principalLeg);
        allTradeLegs.add(maturityLeg);
        allTradeLegs.addAll(interestLegs);
        return allTradeLegs;
    }
}
