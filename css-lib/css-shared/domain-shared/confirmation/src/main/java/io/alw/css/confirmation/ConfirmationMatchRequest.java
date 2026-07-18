package io.alw.css.confirmation;

import io.alw.css.domain.common.TradeType;

import java.util.Set;

public record ConfirmationMatchRequest(
        long requestId,
        Long contraPairId,
        long tradeId,
        int tradeVersion,
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes,
        TradeType tradeType
) implements LongId {

    @Override
    public long id() {
        return requestId;
    }
}
