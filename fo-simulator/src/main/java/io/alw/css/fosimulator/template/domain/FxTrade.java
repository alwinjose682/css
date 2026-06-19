package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;

import java.util.List;

public final class FxTrade implements ExtendedTrade {
    private int nextTradeLegId;
    private TradeLeg side1Msg;
    private TradeLeg side2Msg;
    private Trade trade;

    public FxTrade(TradeType tradeType) {
        super();
        this.nextTradeLegId = resetTradeLegIdProvider();
    }

    @Override
    public int nextTradeLegId() {
        return ++nextTradeLegId;
    }

    @Override
    public int resetTradeLegIdProvider() {
        nextTradeLegId = 0;
        return nextTradeLegId;
    }

    @Override
    public void setTrade(Trade trade) {
        this.trade = trade;
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

    @Override
    public Iterable<TradeDetail> allTradeLegs() {
        return List.of(side1Msg, side2Msg);
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
