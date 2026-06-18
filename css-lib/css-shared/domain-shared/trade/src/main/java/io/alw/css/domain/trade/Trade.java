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
        TransactionType transactionType,
        List<TradeLink> tradeLinks,
        Set<TradeLeg> tradeLegs,
        TradeEventType tradeEventType,
        TradeEventAction tradeEventAction
) implements TestDataGeneratable {
    public static MutableTradeBuilder builder() {
        return new MutableTradeBuilder();
    }

    public Trade clearAndAddTradeLegs(Set<TradeLeg> trdLegs){
        if (!tradeLegs.isEmpty()) {
            tradeLegs.clear();
        }
        tradeLegs.addAll(trdLegs);
        return this;
    }
}
