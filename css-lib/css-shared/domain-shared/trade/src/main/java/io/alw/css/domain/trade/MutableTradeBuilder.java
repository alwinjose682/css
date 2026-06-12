package io.alw.css.domain.trade;

import java.util.HashMap;
import java.util.Map;

public class MutableTradeBuilder extends TradeBuilder {
    private final Map<TradeLegType, TradeLegBuilder> tradeLegBuilders;

    MutableTradeBuilder() {
        this.tradeLegBuilders = new HashMap<>();
    }

    @Override
    public Map<TradeLegType, TradeLeg> tradeLegs() {
        throw new RuntimeException("This method should not be used");
    }

    public MutableTradeBuilder tradeLegs(TradeLegType tradeLegType, TradeLegBuilder tradeLegBuilder) {
        tradeLegBuilders.put(tradeLegType, tradeLegBuilder);
        return this;
    }

    @Override
    public Trade build() {
        Map<TradeLegType, TradeLeg> tradeLegs = new HashMap<>();
        for (var entry : tradeLegBuilders.entrySet()) {
            TradeLeg tradeLeg = entry.getValue().build();
            tradeLegs.put(entry.getKey(), tradeLeg);
        }
        return new Trade(tradeID(), tradeVersion(), tradeType(), bookCode(), counterBookCode(), transactionType(), entityCode(), counterpartyCode(),tradeLinks(),
                tradeLegs);
    }

    public TradeLegBuilder tradeLegs(TradeLegType tradeLegType) {
        return tradeLegBuilders.get(tradeLegType);
    }
}
