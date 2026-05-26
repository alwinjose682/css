package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.TradeLink;

import java.util.List;

public final class FxCashMessageContext implements MessageContext {
    private List<TradeLink> allTradeLinks;
    private FoCashMessage rootFoCashMessage;

    @Override
    public FoCashMessage rootFoCashMessage() {
        return rootFoCashMessage;
    }

    @Override
    public void setRootFoCashMessage(FoCashMessage rootFoCashMessage) {
        this.rootFoCashMessage = rootFoCashMessage;
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
}
