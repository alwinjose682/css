package io.alw.css.fosimulator.template.model;

import io.alw.css.fosimulator.template.MmTemplate;
import io.alw.css.fosimulator.template.domain.TradeMetadata;

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
        private final List<Function<ExtendedTradeLeg, AmendableFoCashMessageField>> amendableFieldSuppliers;

        private AmendableFoCashMessageFieldSupplierBase() {
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFoCashMessageFieldSupplier add(Function<ExtendedTradeLeg, AmendableFoCashMessageField> amendableFieldSupplier) {
            amendableFieldSuppliers.add(amendableFieldSupplier);
            return this;
        }

        public List<Function<ExtendedTradeLeg, AmendableFoCashMessageField>> amendableFieldSupplierFunctions() {
            return amendableFieldSuppliers;
        }
    }

    final class ConditionalSupplier extends AmendableFoCashMessageFieldSupplierBase {
        private final ExtendedTradeLeg conditionSubject;
        private final Predicate<ExtendedTradeLeg> condition;

        public ConditionalSupplier(ExtendedTradeLeg conditionSubject, Predicate<ExtendedTradeLeg> condition) {
            this.conditionSubject = conditionSubject;
            this.condition = condition;
        }

        public Predicate<ExtendedTradeLeg> condition() {
            return condition;
        }

        public ExtendedTradeLeg conditionSubject() {
            return conditionSubject;
        }
    }

    final class SupplierWithMessageSelector extends AmendableFoCashMessageFieldSupplierBase {
        private final TradeMetadata trdCtx;
        private final Function<TradeMetadata, List<? extends ExtendedTradeLeg>> amendmentSubjectSelector;


        public SupplierWithMessageSelector(TradeMetadata trdCtx, Function<TradeMetadata, List<? extends ExtendedTradeLeg>> amendmentSubjectSelector) {
            this.trdCtx = trdCtx;
            this.amendmentSubjectSelector = amendmentSubjectSelector;
        }

        public Function<TradeMetadata, List<? extends ExtendedTradeLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public TradeMetadata trdCtx() {
            return trdCtx;
        }
    }
}
