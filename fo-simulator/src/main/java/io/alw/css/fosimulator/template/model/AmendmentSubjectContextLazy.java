package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.template.domain.CashLeg;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public record AmendmentSubjectContextLazy(
        // Nullable field
        CashLeg cashLeg,
        // Non-nullable fields
        Function<CashLeg, Consumer<FoCashMessage>> callbackProvider,
        Set<AmendableFoCashMessageField> amendableFields) implements AmendmentSubjectContext {

    public AmendmentSubjectContextLazy(Function<CashLeg, Consumer<FoCashMessage>> callbackProvider, Set<AmendableFoCashMessageField> amendableFields) {
        this(null, callbackProvider, amendableFields);
    }

    public AmendmentSubjectContextLazy {
        if (callbackProvider == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null");
        }
    }
}
