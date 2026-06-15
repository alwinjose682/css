package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.fosimulator.template.domain.ExtendedTrade;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

public record TradeLegAmendmentContextEager(
        TradeLegType tradeLegType,
        TradeLeg tradeLeg,
        BiConsumer<? extends ExtendedTrade,TradeLeg> callback,
        Set<AmendableField> amendableFields) implements TradeLegAmendmentContext {

    public TradeLegAmendmentContextEager(TradeLeg tradeLeg, BiConsumer<? extends ExtendedTrade, TradeLeg> callback, Set<AmendableField> amendableFields) {
        this(tradeLeg == null ? null : tradeLeg.tradeLegType(),
                tradeLeg,
                callback,
                amendableFields);
    }

    public TradeLegAmendmentContextEager {
        if (tradeLegType == null || tradeLeg == null || callback == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null. If some values are not known, use AmendmentSubjectContextLazy.class");
        }
        amendableFields = Collections.unmodifiableSet(amendableFields);
    }
}
