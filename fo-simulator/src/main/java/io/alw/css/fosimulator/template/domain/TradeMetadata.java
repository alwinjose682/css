package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.datagen.TestDataGeneratable;

public sealed interface TradeMetadata extends TestDataGeneratable permits FxTrade, MmTrade {
    TradeType tradeType();

    TradeLeg rootTradeLeg();

    void setRootTradeLeg(TradeLeg rootTradeLeg);

    int nextTradeLegId();
}
