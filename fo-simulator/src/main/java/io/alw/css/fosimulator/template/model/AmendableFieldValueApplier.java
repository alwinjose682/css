package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface AmendableFieldValueApplier {
    record ConditionalApplier(Predicate<FoCashMessage> condition) implements AmendableFieldValueApplier { }
    record ConditionalApplierWithMessageSelector(Supplier<List<FoCashMessage>> amendmentSubjectSelector, Predicate<FoCashMessage> condition) implements AmendableFieldValueApplier { }
    record ConditionalApplierWithMessageSelectorAndSideEffectAction(Supplier<List<FoCashMessage>> amendmentSubjectSelector, Predicate<FoCashMessage> condition, Consumer<List<FoCashMessage>> sideEffectAction) implements AmendableFieldValueApplier { }
    record DirectApplier() implements AmendableFieldValueApplier { }
}
