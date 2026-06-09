package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.*;

import java.util.List;

public final class MmTradeContext implements TradeContext {
    private final TradeType tradeType;
    private final MmCashLeg principalLeg;
    private MmCashLeg maturityLeg;
    private final List<InterestCashLeg> interestLegs;

    public MmTradeContext(TradeType tradeType, MmCashLeg principalLeg, List<InterestCashLeg> interestLegs) {
        this(tradeType, principalLeg, interestLegs, null);
    }

    public MmTradeContext(TradeType tradeType, MmCashLeg principalLeg, List<InterestCashLeg> interestLegs, MmCashLeg maturityLeg) {
        if (interestLegs == null) {
            throw new RuntimeException("Must have at least one interest leg to construct MmTradeContext");
        }
        // Assign this TradeContext reference to all the cashLegs
        principalLeg.setTradeContext(this);
        interestLegs.forEach(il -> il.setTradeContext(this));
        if (maturityLeg != null) {
            maturityLeg.setTradeContext(this);
        }

        this.tradeType = tradeType;
        this.principalLeg = principalLeg;
        this.interestLegs = interestLegs;
        this.maturityLeg = maturityLeg;
    }

    @Override
    public TradeType tradeType() {
        return tradeType;
    }

    @Override
    public FoCashMessage rootFoCashMessage() {
        return principalLeg.cashMessage();
    }

    @Override
    public void setRootFoCashMessage(FoCashMessage rootFoCashMessage) {
        principalLeg.setCashMessage(rootFoCashMessage);
    }

    public MmCashLeg principalLeg() {
        return principalLeg;
    }

    public MmCashLeg maturityLeg() {
        return maturityLeg;
    }

    public void setMaturityLeg(MmCashLeg maturityLeg) {
        this.maturityLeg = maturityLeg;
    }

    public List<InterestCashLeg> interestLegs() {
        return interestLegs;
    }
}
