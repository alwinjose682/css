package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;

import java.util.List;

public final class FxTradeContext implements TradeContext {
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
    public <M extends TradeContext> List<FoCashMessage> mapToCashMessage(List<M> trdCtxs) {

    }
}
