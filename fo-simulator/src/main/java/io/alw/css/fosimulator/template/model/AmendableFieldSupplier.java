package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeLeg;
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
public sealed interface AmendableFieldSupplier extends AmendableField {

    sealed abstract class AmendableFieldSupplierBase implements AmendableFieldSupplier {
        private final List<Function<TradeLeg, AmendableField>> amendableFieldSuppliers;

        private AmendableFieldSupplierBase() {
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFieldSupplier add(Function<TradeLeg, AmendableField> amendableFieldSupplier) {
            amendableFieldSuppliers.add(amendableFieldSupplier);
            return this;
        }

        public List<Function<TradeLeg, AmendableField>> amendableFieldSupplierFunctions() {
            return amendableFieldSuppliers;
        }
    }

    final class ConditionalSupplier extends AmendableFieldSupplierBase {
        private final TradeLeg conditionSubject;
        private final Predicate<TradeLeg> condition;

        public ConditionalSupplier(TradeLeg conditionSubject, Predicate<TradeLeg> condition) {
            this.conditionSubject = conditionSubject;
            this.condition = condition;
        }

        public Predicate<TradeLeg> condition() {
            return condition;
        }

        public TradeLeg conditionSubject() {
            return conditionSubject;
        }
    }

    final class SupplierWithMessageSelector extends AmendableFieldSupplierBase {
        private final TradeMetadata trdCtx;
        private final Function<TradeMetadata, List<? extends TradeLeg>> amendmentSubjectSelector;


        public SupplierWithMessageSelector(TradeMetadata trdCtx, Function<TradeMetadata, List<? extends TradeLeg>> amendmentSubjectSelector) {
            this.trdCtx = trdCtx;
            this.amendmentSubjectSelector = amendmentSubjectSelector;
        }

        public Function<TradeMetadata, List<? extends TradeLeg>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public TradeMetadata trdCtx() {
            return trdCtx;
        }
    }
}
