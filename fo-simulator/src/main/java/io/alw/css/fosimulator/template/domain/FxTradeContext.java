package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.common.TradeType;

public final class FxTradeContext implements TradeContext {
    private final TradeType tradeType;
    private FoCashMessage side1Msg;
    private FoCashMessage side2Msg;

    public FxTradeContext(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public TradeType tradeType() {
        return tradeType;
    }

    @Override
    public FoCashMessage rootFoCashMessage() {
        return side1Msg;
    }

    @Override
    public void setRootFoCashMessage(FoCashMessage rootFoCashMessage) {
        this.side1Msg = rootFoCashMessage;
    }

    public FoCashMessage side2Msg() {
        return side2Msg;
    }

    public void setSide2Msg(FoCashMessage side2Msg) {
        this.side2Msg = side2Msg;
    }

    public FoCashMessage side1Msg() {
        return side1Msg;
    }

    public void setSide1Msg(FoCashMessage side1Msg) {
        this.side1Msg = side1Msg;
    }
}
