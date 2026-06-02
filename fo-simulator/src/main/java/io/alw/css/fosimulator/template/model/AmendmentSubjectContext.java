package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;

import java.util.Set;
import java.util.function.Consumer;

public record AmendmentSubjectContext(
        CashLeg amendmentSubject,
        Consumer<FoCashMessage> callback,
        Set<AmendableFoCashMessageField> amendableFields) {
}
