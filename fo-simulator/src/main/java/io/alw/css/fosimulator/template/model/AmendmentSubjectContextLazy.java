package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.template.domain.CashLeg;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public record AmendmentSubjectContextLazy(
        Function<CashLeg, Consumer<FoCashMessage>> callbackProvider,
        Set<AmendableFoCashMessageField> amendableFields) implements AmendmentSubjectContext {

    public AmendmentSubjectContextLazy {
        if (callbackProvider == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null");
        }
    }
}
