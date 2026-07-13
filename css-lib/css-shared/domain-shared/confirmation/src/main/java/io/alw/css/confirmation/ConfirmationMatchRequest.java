package io.alw.css.confirmation;

import io.alw.css.domain.common.TradeType;

import java.util.Set;

public record ConfirmationMatchRequest(
        long requestId,
        long contraPairId,
        long tradeId,
        int tradeVersion,
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes,
        TradeType tradeType
) {
}
