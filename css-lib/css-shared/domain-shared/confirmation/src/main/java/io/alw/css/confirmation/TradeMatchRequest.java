package io.alw.css.confirmation;

import io.alw.css.domain.common.TradeType;

import java.util.List;

public record TradeMatchRequest(
        long tradeId,
        int tradeVersion,
        List<TradeLegMatchAttribute> tradeLegMatchAttributes,
        TradeType tradeType
) {
}
