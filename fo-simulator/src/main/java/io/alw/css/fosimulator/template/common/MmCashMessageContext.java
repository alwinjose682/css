package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.*;

import java.math.BigDecimal;
import java.util.List;

public final class MmCashMessageContext implements MessageContext {
    private final MmCashLeg principal;
    private MmCashLeg maturity;
    private final List<InterestCashLeg> interests;
    private List<TradeLink> allTradeLinks;

    public MmCashMessageContext(MmCashLeg principal, List<InterestCashLeg> interests) {
        this(principal, interests, null);
    }

    public MmCashMessageContext(MmCashLeg principal, List<InterestCashLeg> interests, MmCashLeg maturity) {
        this.principal = principal;
        this.interests = interests;
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
    public List<TradeLink> allTradeLinks() {
        return allTradeLinks;
    }

    @Override
    public void setAllTradeLinks(List<TradeLink> allTradeLinks) {
        this.allTradeLinks = allTradeLinks;
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
