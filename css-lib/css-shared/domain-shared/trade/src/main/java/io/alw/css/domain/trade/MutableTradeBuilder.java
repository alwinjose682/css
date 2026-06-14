package io.alw.css.domain.trade;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MutableTradeBuilder extends TradeBuilder {
    private final Set<TradeLegBuilder> tradeLegBuilders;

    MutableTradeBuilder() {
        this.tradeLegBuilders = new HashSet<>();
    }

    @Override
    public Set<TradeLeg> tradeLegs() {
        throw new RuntimeException("This method should not be used");
    }

    public MutableTradeBuilder tradeLegs(TradeLegBuilder tradeLegBuilder) {
        tradeLegBuilders.add(tradeLegBuilder);
        return this;
    }

    @Override
    public Trade build() {
        var tradeLegs = tradeLegBuilders.stream().map(TradeLegBuilder::build).collect(Collectors.toSet());
        return new Trade(tradeID(), tradeVersion(), tradeType(), transactionType(), tradeLinks(),
                tradeLegs,
                tradeEventType(), tradeEventAction());
    }
}
