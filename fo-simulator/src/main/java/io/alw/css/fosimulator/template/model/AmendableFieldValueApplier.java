package io.alw.css.fosimulator.template.model;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface AmendableFieldValueApplier {
    record ConditionalApplier(
            Predicate<? extends CashLeg> condition,
            Function<? extends CashLeg, AmendableFoCashMessageField> fieldWithNewValueObtainer) implements AmendableFieldValueApplier {
    }

    record ApplierWithMessageSelector(Supplier<List<? extends CashLeg>> amendmentSubjectSelector,
                                      Function<? extends CashLeg, AmendableFoCashMessageField> fieldWithNewValueObtainer) implements AmendableFieldValueApplier {
    }

    record ConditionalApplierWithMessageSelectorAndSideEffectAction(Supplier<List<? extends CashLeg>> amendmentSubjectSelector,
                                                                    Predicate<? extends CashLeg> condition,
                                                                    Function<? extends CashLeg, AmendableFoCashMessageField> fieldWithNewValueObtainer,
                                                                    Consumer<List<? extends CashLeg>> sideEffectAction) implements AmendableFieldValueApplier {
    }

    record DirectApplier() implements AmendableFieldValueApplier {
    }
}
