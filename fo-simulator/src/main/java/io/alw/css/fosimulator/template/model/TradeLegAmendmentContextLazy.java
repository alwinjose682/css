package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public record TradeLegAmendmentContextLazy(
        // Nullable field
        TradeLeg tradeLeg,
        // Non-nullable fields
        Function<TradeDetail, Consumer<TradeLeg>> callbackProvider,
        Set<AmendableField> amendableFields) implements TradeLegAmendmentContext {

    public TradeLegAmendmentContextLazy(Function<TradeDetail, Consumer<TradeLeg>> callbackProvider, Set<AmendableField> amendableFields) {
        this(null, callbackProvider, amendableFields);
    }

    public TradeLegAmendmentContextLazy {
        if (callbackProvider == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null");
        }
    }
}
