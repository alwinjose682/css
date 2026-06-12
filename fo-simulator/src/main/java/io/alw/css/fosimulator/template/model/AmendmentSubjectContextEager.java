package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.fosimulator.template.domain.CashLeg;

import java.util.Set;
import java.util.function.Consumer;

public record AmendmentSubjectContextEager(
        TradeLegType tradeLegType,
        FoCashMessage amendmentSubject,
        Consumer<FoCashMessage> callback,
        Set<AmendableFoCashMessageField> amendableFields) implements AmendmentSubjectContext {

    public AmendmentSubjectContextEager(CashLeg cashLeg, Consumer<FoCashMessage> callback, Set<AmendableFoCashMessageField> amendableFields) {
        this(cashLeg == null ? null : cashLeg.cashLegType(),
                cashLeg == null ? null : cashLeg.cashMessage(),
                callback,
                amendableFields);
    }

    public AmendmentSubjectContextEager {
        if (tradeLegType == null || amendmentSubject == null || callback == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null. If some values are not known, use AmendmentSubjectContextLazy.class");
        }
    }
}
