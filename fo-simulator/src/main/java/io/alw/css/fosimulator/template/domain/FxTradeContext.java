package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLeg;

public final class FxTradeContext extends Trade implements TradeMetadata {
    private int nextTradeLegId;
    private TradeLeg side1Msg;
    private TradeLeg side2Msg;
    private Trade trade;

    public FxTradeContext(TradeType tradeType) {
        this.nextTradeLegId = 0;
        super();
    }

    @Override
    public int nextTradeLegId() {
        return ++nextTradeLegId;
    }

    @Override
    public TradeLeg rootTradeLeg() {
        return side1Msg;
    }

    @Override
    public void setRootTradeLeg(TradeLeg rootFoCashMessage) {
        this.side1Msg = rootFoCashMessage;
    }

    @Override
    public Trade trade() {
        return trade;
    }

    public TradeLeg side2Msg() {
        return side2Msg;
    }

    public void setSide2Msg(TradeLeg side2Msg) {
        this.side2Msg = side2Msg;
    }

    public TradeLeg side1Msg() {
        return side1Msg;
    }

    public void setSide1Msg(TradeLeg side1Msg) {
        this.side1Msg = side1Msg;
    }
}
