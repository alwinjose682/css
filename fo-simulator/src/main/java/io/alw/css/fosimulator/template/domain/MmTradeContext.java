package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.RateType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegBuilder;

import java.util.ArrayList;
import java.util.List;

public final class MmTradeContext implements TradeMetadata {
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;
    private Trade trade;

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

    @Override
    public Iterable<TradeLeg> allTradeLegsInOrderOfImportance() {
        var allTradeLegs = new ArrayList<TradeLeg>();
        allTradeLegs.add(principalLeg);
        allTradeLegs.add(maturityLeg);
        allTradeLegs.addAll(interestLegs);
        return allTradeLegs;
    }

    @Override
    public TradeLegBuilder getSuitableBuilderFrom(TradeLeg trdLeg) {
        return switch (trdLeg) {
            case InterestTradeLeg itl -> InterestTradeLegBuilder.builder(itl);
            case TradeLeg tl -> TradeLegBuilder.builder(trdLeg);
            default -> throw new RuntimeException("The given TradeLeg of type: " + trdLeg.tradeLegType() + " is not an MM Trade leg");
        };
    }
}
