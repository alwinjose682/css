package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MmCashMessageContext implements MessageContext {
    private final MmCashLeg principal;
    private MmCashLeg maturity;
    private final List<InterestCashLeg> interests;
    private final Map<String, List<TradeLink>> allTradeLinks;

    public MmCashMessageContext(MmCashLeg principal, List<InterestCashLeg> interests) {
        this(principal, interests, null);
    }

    public MmCashMessageContext(MmCashLeg principal, List<InterestCashLeg> interests, MmCashLeg maturity) {
        this.principal = principal;
        this.interests = interests;
        this.allTradeLinks = new HashMap<>();
    }

    @Override
    public FoCashMessage rootFoCashMessage() {
        return principal.cashMessage();
    }

    @Override
    public void setRootFoCashMessage(FoCashMessage rootFoCashMessage) {
        principal.setCashMessage(rootFoCashMessage);
    }

    @Override
    public <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs) {

    }

    @Override
    public Map<String, List<TradeLink>> allTradeLinks() {
        return allTradeLinks;
    }

    public MmCashLeg principal() {
        return principal;
    }

    public MmCashLeg maturity() {
        return maturity;
    }

    public void setMaturity(MmCashLeg maturity) {
        this.maturity = maturity;
    }

    public List<InterestCashLeg> interests() {
        return interests;
    }
}
