package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.template.domain.CashLeg;
import io.alw.css.fosimulator.template.domain.CashLegType;

import java.util.Set;
import java.util.function.Consumer;

public record AmendmentSubjectContextEager(
        CashLegType cashLegType,
        FoCashMessage amendmentSubject,
        Consumer<FoCashMessage> callback,
        Set<AmendableFoCashMessageField> amendableFields) implements AmendmentSubjectContext {

    public AmendmentSubjectContextEager {
        if (cashLegType==null || amendmentSubject == null || callback == null || amendableFields == null) {
            throw new RuntimeException("No part of the state of this class can be null. If some values are not known, use AmendmentSubjectContextLazy.class");
        }
    }
}
