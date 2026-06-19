package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;

import java.util.List;

public final class FxTrade implements ExtendedTrade {
    private int nextTradeLegId;
    private Trade trade;
    private TradeLeg tradeLeg1;
    private TradeLeg tradeLeg2;

    public FxTrade() {
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
        return tradeLeg1;
    }

    @Override
    public void setRootTradeLeg(TradeLeg rootFoCashMessage) {
        this.tradeLeg1 = rootFoCashMessage;
    }

    @Override
    public Trade trade() {
        return trade;
    }

    @Override
    public Iterable<TradeDetail> allTradeLegs() {
        return List.of(tradeLeg1, tradeLeg2);
    }

    public TradeLeg tradeLeg2() {
        return tradeLeg2;
    }

    public void setTradeLeg2(TradeLeg tradeLeg2) {
        this.tradeLeg2 = tradeLeg2;
    }

    public TradeLeg tradeLeg1() {
        return tradeLeg1;
    }

    public void setTradeLeg1(TradeLeg tradeLeg1) {
        this.tradeLeg1 = tradeLeg1;
    }
}
