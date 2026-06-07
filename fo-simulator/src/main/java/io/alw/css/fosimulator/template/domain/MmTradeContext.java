package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.*;

import java.util.List;

public final class MmTradeContext implements TradeContext {
    private final MmCashLeg principalLeg;
    private MmCashLeg maturityLeg;
    private final List<InterestCashLeg> interestLegs;

    public MmTradeContext(MmCashLeg principalLeg, List<InterestCashLeg> interestLegs) {
        this(principalLeg, interestLegs, null);
    }

    public MmTradeContext(MmCashLeg principalLeg, List<InterestCashLeg> interestLegs, MmCashLeg maturityLeg) {
        this.principalLeg = principalLeg;
        this.interestLegs = interestLegs;
    }

    @Override
    public FoCashMessage rootFoCashMessage() {
        return principalLeg.cashMessage();
    }

    @Override
    public void setRootFoCashMessage(FoCashMessage rootFoCashMessage) {
        principalLeg.setCashMessage(rootFoCashMessage);
    }

    @Override
    public <M extends TradeContext> List<FoCashMessage> mapToCashMessage(List<M> trdCtxs) {

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
