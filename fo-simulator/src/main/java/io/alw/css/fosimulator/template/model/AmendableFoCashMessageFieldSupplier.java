package io.alw.css.fosimulator.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

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

        public Function<? extends CashLeg, AmendableFoCashMessageField> amendableFieldSupplier() {
            return amendableFieldSupplier;
        }

        public CashLeg conditionSubject() {
            return conditionSubject;
        }
    }

    final class SupplierWithMessageSelector implements AmendableFoCashMessageFieldSupplier {
        private final MessageContext msgCtx;
        private final Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector;
        private final List<Function<CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers;

        public SupplierWithMessageSelector(MessageContext msgCtx, Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector) {
            this.msgCtx = msgCtx;
            this.amendmentSubjectSelector = amendmentSubjectSelector;
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFoCashMessageFieldSupplier add(Function<CashLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            amendableFieldSuppliers.add(amendableFieldSupplier);
            return this;
        }

        public Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public List<Function<CashLeg, AmendableFoCashMessageField>> amendableFieldSupplierFunctions() {
            return amendableFieldSuppliers;
        }

        public MessageContext msgCtx() {
            return msgCtx;
        }
    }

    final class ConditionalSupplierWithMessageSelector implements AmendableFoCashMessageFieldSupplier {
        private final MessageContext msgCtx;
        private final Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector;
        private final Predicate<? extends CashLeg> condition;
        private final List<Function<CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers;

        public ConditionalSupplierWithMessageSelector(MessageContext msgCtx, Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector,
                                                      Predicate<? extends CashLeg> condition) {
            this.msgCtx = msgCtx;
            this.amendmentSubjectSelector = amendmentSubjectSelector;
            this.condition = condition;
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFoCashMessageFieldSupplier add(Function<CashLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            amendableFieldSuppliers.add(amendableFieldSupplier);
            return this;
        }

        public Function<MessageContext, List<? extends CashLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public Predicate<? extends CashLeg> condition() {
            return condition;
        }

        public List<Function<CashLeg, AmendableFoCashMessageField>> amendableFieldSupplierFunctions() {
            return amendableFieldSuppliers;
        }

        public MessageContext msgCtx() {
            return msgCtx;
        }
    }
}
