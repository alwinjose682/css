package io.alw.css.fosimulator.template.model;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public record AmendmentSubjectContextLazy(
        // Nullable field
        ExtendedTradeLeg cashLeg,
        // Non-nullable fields
        Function<ExtendedTradeLeg, Consumer<Trade>> callbackProvider,
        Set<AmendableFoCashMessageField> amendableFields) implements AmendmentSubjectContext {

    public AmendmentSubjectContextLazy(Function<ExtendedTradeLeg, Consumer<Trade>> callbackProvider, Set<AmendableFoCashMessageField> amendableFields) {
        this(null, callbackProvider, amendableFields);
    }

    public AmendmentSubjectContextLazy {
        if (callbackProvider == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null");
        }
    }
}
