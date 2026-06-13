package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.datagen.TestDataGeneratable;

public sealed interface TradeMetadata extends TestDataGeneratable permits FxTradeContext, MmTradeContext {
    int nextTradeLegId();

    TradeLeg rootTradeLeg();

    void setRootTradeLeg(TradeLeg rootTradeLeg);

    Trade trade();

    default long tradeId() {
        return trade().tradeID();
    }

    default int tradeVersion() {
        return trade().tradeVersion();
    }

    default TradeEventType tradeEventType() {
        return trade().tradeEventType();
    }

    Iterable<TradeLeg> allTradeLegs();
}
