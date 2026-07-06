package io.alw.css.tradepublisher.trade.template.domain;

import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;

import java.util.List;
import java.util.Objects;

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
    public void setRootTradeLeg(TradeLeg rootTradeLeg) {
        this.tradeLeg1 = rootTradeLeg;
    }

    @Override
    public Trade trade() {
        return trade;
    }

    @Override
    public Iterable<TradeLeg> allTradeLegs() {
        return List.of(tradeLeg1, tradeLeg2);
    }

    @Override
    public TradeLeg getTradeLegFrom(TradeDetail tradeDetail) {
        // Safe to cast without any checks. Unlike MmTrade that has InterestTradeLeg that extends tradeDetail, FxTrade only has concrete TradeLegs
        return (TradeLeg) tradeDetail;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FxTrade fxTrade = (FxTrade) o;
        return Objects.equals(trade, fxTrade.trade);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(trade);
    }
}
