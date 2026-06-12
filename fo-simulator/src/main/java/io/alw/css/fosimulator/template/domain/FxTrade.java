package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.trade.Trade;

public final class FxTrade implements TradeMetadata {
    private final TradeType tradeType;
    private Trade side1Msg;
    private Trade side2Msg;

    public FxTrade(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public TradeType tradeType() {
        return tradeType;
    }

    @Override
    public Trade rootTradeLeg() {
        return side1Msg;
    }

    @Override
    public void setRootTradeLeg(Trade rootFoCashMessage) {
        this.side1Msg = rootFoCashMessage;
    }

    public Trade side2Msg() {
        return side2Msg;
    }

    public void setSide2Msg(Trade side2Msg) {
        this.side2Msg = side2Msg;
    }

    public Trade side1Msg() {
        return side1Msg;
    }

    public void setSide1Msg(Trade side1Msg) {
        this.side1Msg = side1Msg;
    }
}
