package io.alw.css.fosimulator.template.model;

import io.alw.css.fosimulator.template.MmTemplate;
import io.alw.css.fosimulator.template.domain.CashLeg;
import io.alw.css.fosimulator.template.domain.TradeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/// NOTE: These amendable field supplier classes contain both:
/// !) the functions that compute the value and
/// 2) the parameters(object references, not the actual parameter value) necessary for the function to compute the value
/// Normally, this is not the case where functions need to be used. It is used only to facilitate lazy build of the amendment messages.
/// Check the use site of this class for context(example: [MmTemplate#buildAmendmentContextForPrimarySubjectPrincipal])
public sealed interface AmendableFoCashMessageFieldSupplier extends AmendableFoCashMessageField {

    sealed abstract class AmendableFoCashMessageFieldSupplierBase implements AmendableFoCashMessageFieldSupplier {
        private final List<Function<CashLeg, AmendableFoCashMessageField>> amendableFieldSuppliers;

        private AmendableFoCashMessageFieldSupplierBase() {
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFoCashMessageFieldSupplier add(Function<CashLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            amendableFieldSuppliers.add(amendableFieldSupplier);
            return this;
        }

        public List<Function<CashLeg, AmendableFoCashMessageField>> amendableFieldSupplierFunctions() {
            return amendableFieldSuppliers;
        }
    }

    final class ConditionalSupplier extends AmendableFoCashMessageFieldSupplierBase {
        private final CashLeg conditionSubject;
        private final Predicate<CashLeg> condition;

        public ConditionalSupplier(CashLeg conditionSubject, Predicate<CashLeg> condition) {
            this.conditionSubject = conditionSubject;
            this.condition = condition;
        }

        public Predicate<CashLeg> condition() {
            return condition;
        }

        public CashLeg conditionSubject() {
            return conditionSubject;
        }
    }

    final class SupplierWithMessageSelector extends AmendableFoCashMessageFieldSupplierBase {
        private final TradeContext trdCtx;
        private final Function<TradeContext, List<? extends CashLeg>> amendmentSubjectSelector;


        public SupplierWithMessageSelector(TradeContext trdCtx, Function<TradeContext, List<? extends CashLeg>> amendmentSubjectSelector) {
            this.trdCtx = trdCtx;
            this.amendmentSubjectSelector = amendmentSubjectSelector;
        }

        public Function<TradeContext, List<? extends CashLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public TradeContext trdCtx() {
            return trdCtx;
        }
    }
}
