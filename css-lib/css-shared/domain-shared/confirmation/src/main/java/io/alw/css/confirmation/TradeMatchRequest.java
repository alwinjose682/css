package io.alw.css.confirmation;

import io.alw.css.domain.common.TradeType;

import java.util.Set;

public record TradeMatchRequest(
        long tradeId,
        int tradeVersion,
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes,
        TradeType tradeType
) {
}
