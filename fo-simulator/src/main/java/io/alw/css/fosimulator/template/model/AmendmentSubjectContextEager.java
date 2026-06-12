package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLegType;

import java.util.Set;
import java.util.function.Consumer;

public record AmendmentSubjectContextEager(
        TradeLegType tradeLegType,
        Trade amendmentSubject,
        Consumer<Trade> callback,
        Set<AmendableFoCashMessageField> amendableFields) implements AmendmentSubjectContext {

    public AmendmentSubjectContextEager(ExtendedTradeLeg cashLeg, Consumer<Trade> callback, Set<AmendableFoCashMessageField> amendableFields) {
        this(cashLeg == null ? null : cashLeg.tradeLegType(),
                cashLeg == null ? null : cashLeg.tradeLeg(),
                callback,
                amendableFields);
    }

    public AmendmentSubjectContextEager {
        if (tradeLegType == null || amendmentSubject == null || callback == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null. If some values are not known, use AmendmentSubjectContextLazy.class");
        }
    }
}
