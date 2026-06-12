package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.RateType;
import io.alw.css.domain.common.TradeLink;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MmTrade extends Trade implements TradeMetadata {
    private final TradeType tradeType;
    private final RateType rateType;
    private final InterestPayoutFrequency ipFrequency;
    private final InterestBasis interestBasis;

    private int nextTradeLegId;
    private TradeLeg principalLeg;
    private TradeLeg maturityLeg;
    private final List<InterestTradeLeg> interestLegs;

    public MmTrade(TradeType tradeType, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
        this.rateType = rateType;
        this.ipFrequency = ipFrequency;
        this.interestBasis = interestBasis;

        this.nextTradeLegId = 0;
        this.tradeType = tradeType;
        this.interestLegs = new ArrayList<>();
    }

    public MmTrade(long tradeID, int tradeVersion, TradeType tradeType, String bookCode, String counterBookCode, TransactionType transactionType, String entityCode, String counterpartyCode, List<TradeLink> tradeLinks, Map<TradeLegType, TradeLeg> tradeLegs) {
        super(tradeID, tradeVersion, tradeType, bookCode, counterBookCode, transactionType, entityCode, counterpartyCode, tradeLinks, tradeLegs);
    }


    @Override
    public TradeType tradeType() {
        return tradeType;
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
}
