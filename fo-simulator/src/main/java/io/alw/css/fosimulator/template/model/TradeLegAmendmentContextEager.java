package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegType;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public record TradeLegAmendmentContextEager(
        TradeLegType tradeLegType,
        TradeLeg tradeLeg,
        Consumer<TradeLeg> callback,
        Set<AmendableField> amendableFields) implements TradeLegAmendmentContext {

    public TradeLegAmendmentContextEager(TradeLeg tradeLeg, Consumer<TradeLeg> callback, Set<AmendableField> amendableFields) {
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
