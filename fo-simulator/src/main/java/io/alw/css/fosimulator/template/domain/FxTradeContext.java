package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;

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
}
