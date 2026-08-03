package io.alw.css.tradepublisher.trade.template.domain;

import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.tradepublisher.trade.model.TradeEventActionPair;

public record TradeLegGenerationSchedule(
        long scheduleDay,
        TradeLegType tradeLegType,
        TradeEventActionPair tradeEventActionPair
) {
}
