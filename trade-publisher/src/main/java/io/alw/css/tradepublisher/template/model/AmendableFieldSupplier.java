package io.alw.css.tradepublisher.template.model;

import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.tradepublisher.template.MmTemplate;
import io.alw.css.tradepublisher.template.domain.ExtendedTrade;

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
        private final List<Function<TradeDetail, AmendableField>> amendableFieldSuppliers;

        private AmendableFieldSupplierBase() {
            this.amendableFieldSuppliers = new ArrayList<>();
        }

        public AmendableFieldSupplier add(Function<TradeDetail, AmendableField> amendableFieldSupplier) {
            amendableFieldSuppliers.add(amendableFieldSupplier);
            return this;
        }

        public List<Function<TradeDetail, AmendableField>> amendableFieldSupplierFunctions() {
            return amendableFieldSuppliers;
        }
    }

    final class ConditionalSupplier extends AmendableFieldSupplierBase {
        private final TradeLeg conditionSubject;
        private final Predicate<TradeDetail> condition;

        public ConditionalSupplier(TradeLeg conditionSubject, Predicate<TradeDetail> condition) {
            this.conditionSubject = conditionSubject;
            this.condition = condition;
        }

        public Predicate<TradeDetail> condition() {
            return condition;
        }

        public TradeDetail conditionSubject() {
            return conditionSubject;
        }
    }

    final class SupplierWithMessageSelector extends AmendableFieldSupplierBase {
        private final ExtendedTrade trdCtx;
        private final Function<ExtendedTrade, List<? extends TradeDetail>> amendmentSubjectSelector;


        public SupplierWithMessageSelector(ExtendedTrade trdCtx, Function<ExtendedTrade, List<? extends TradeDetail>> amendmentSubjectSelector) {
            this.trdCtx = trdCtx;
            this.amendmentSubjectSelector = amendmentSubjectSelector;
        }

        public Function<ExtendedTrade, List<? extends TradeDetail>> amendmentSubjectSelector() {
            return amendmentSubjectSelector;
        }

        public ExtendedTrade trdCtx() {
            return trdCtx;
        }
    }
}
