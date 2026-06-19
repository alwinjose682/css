package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.datagen.DataGeneratable;

public sealed interface ExtendedTrade extends DataGeneratable permits FxTrade, MmTrade {
    int nextTradeLegId();

    int resetTradeLegIdProvider();

    void setTrade(Trade trade);

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

    Iterable<TradeDetail> allTradeLegs();
}
