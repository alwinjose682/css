package io.alw.css.fosimulator.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface AmendableFoCashMessageFieldSupplier extends AmendableFoCashMessageField {
    final class ConditionalSupplier implements AmendableFoCashMessageFieldSupplier {
        private final CashLeg conditionSubject;
        private final Predicate<? extends CashLeg> condition;
        private final Function<? extends CashLeg, AmendableFoCashMessageField> amendableFieldSupplier;

        public ConditionalSupplier(CashLeg conditionSubject, Predicate<? extends CashLeg> condition, Function<? extends CashLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            this.conditionSubject = conditionSubject;
            this.condition = condition;
            this.amendableFieldSupplier = amendableFieldSupplier;
        }

        public Predicate<? extends CashLeg> condition() {
            return condition;
        }

        public Function<? extends CashLeg, AmendableFoCashMessageField> amendableFieldSuppliers() {
            return amendableFieldSupplier;
        }
    }

    final class SupplierWithMessageSelector implements AmendableFoCashMessageFieldSupplier {
        private final Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector;
        private final List<Function<? extends CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers;

        public SupplierWithMessageSelector(Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector) {
            this.amendmentSubjectSelector = amendmentSubjectSelector;
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFoCashMessageFieldSupplier add(Function<? extends CashLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            amendableFieldSuppliers().add(amendableFieldSupplier);
            return this;
        }

        public Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public List<Function<? extends CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers() {
            return amendableFieldSuppliers;
        }
    }

    final class ConditionalSupplierWithMessageSelector implements AmendableFoCashMessageFieldSupplier {
        private final Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector;
        private final Predicate<? extends CashLeg> condition;
        private final List<Function<? extends CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers;

        public ConditionalSupplierWithMessageSelector(Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector,
                                                      Predicate<? extends CashLeg> condition) {
            this.amendmentSubjectSelector = amendmentSubjectSelector;
            this.condition = condition;
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFoCashMessageFieldSupplier add(Function<? extends CashLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            amendableFieldSuppliers().add(amendableFieldSupplier);
            return this;
        }

        public Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public Predicate<? extends CashLeg> condition() {
            return condition;
        }

        public List<Function<? extends CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers() {
            return amendableFieldSuppliers;
        }
    }
}
