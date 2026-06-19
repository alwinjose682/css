package io.alw.css.domain.trade;

import io.alw.css.domain.common.*;
import io.alw.datagen.DataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;
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
) implements DataGeneratable {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return tradeID == trade.tradeID && tradeVersion == trade.tradeVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeID, tradeVersion);
    }
}
