package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.*;

import java.util.List;

public final class MmCashMessageContext implements MessageContext {
    private final MmCashLeg principalLeg;
    private MmCashLeg maturityLeg;
    private final List<InterestCashLeg> interestLegs;

    public MmCashMessageContext(MmCashLeg principalLeg, List<InterestCashLeg> interestLegs) {
        this(principalLeg, interestLegs, null);
    }

    public MmCashMessageContext(MmCashLeg principalLeg, List<InterestCashLeg> interestLegs, MmCashLeg maturityLeg) {
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
    public <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs) {

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
