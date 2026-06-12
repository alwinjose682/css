package io.alw.css.domain.trade;

import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@RecordBuilder
public record Trade(
        long tradeID,
        int tradeVersion,
        @NotNull TradeType tradeType,
        String bookCode,
        String counterBookCode, // Can be null if not an internal trade
        TransactionType transactionType,
        @NotNull String entityCode,
        @NotNull String counterpartyCode,
        Set<TradeLeg> tradeLegs
) {
}
