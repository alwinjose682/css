package io.alw.css.domain.trade;

import io.alw.css.domain.common.*;
import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

@RecordBuilder
@RecordBuilder.Options(publicBuilderConstructors = true)
public record Trade(
        long tradeID,
        int tradeVersion,
        @NotNull TradeType tradeType,
        String bookCode,
        String counterBookCode, // Can be null if not an internal trade
        TransactionType transactionType,
        @NotNull String entityCode,
        @NotNull String counterpartyCode,
        List<TradeLink> tradeLinks,
        Set<TradeLeg> tradeLegs,
        TradeEventType tradeEventType,
        TradeEventAction tradeEventAction
) implements TestDataGeneratable {
    public static MutableTradeBuilder builder() {
        return new MutableTradeBuilder();
    }
}
