package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.RateType;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.fosimulator.template.domain.InterestPayoutFrequency;
import io.alw.css.fosimulator.template.model.AmendableFieldType;
import io.alw.datagen.provider.AbstractCyclicDataProvider;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static io.alw.css.domain.common.RateType.FIXED;
import static io.alw.css.domain.common.RateType.FLOAT;
import static io.alw.css.domain.trade.TradeLegType.MM_MATURITY;
import static io.alw.css.domain.trade.TradeLegType.MM_PRINCIPAL;
import static io.alw.css.fosimulator.template.domain.InterestPayoutFrequency.*;
import static io.alw.css.fosimulator.template.model.AmendableFieldType.*;

public final class MmTemplateConstants {
    static final double principalLegAmountOrigin = 100000;
    static final double principalLegAmountBound = 98500000;

    static final Supplier<RateType> cyclicRateTypeProvider = new CyclicRateTypeProvider(List.of(FIXED, FLOAT, FIXED, FLOAT));
    static final Supplier<InterestPayoutFrequency> cyclicIpFrequencyProvider = new CyclicInterestPayoutFrequencyProvider(List.of(DAY, MONTHLY, MONTHLY, MONTHLY, PRINCIPAL_MATURITY, MONTHLY, QUARTERLY, QUARTERLY, SEMI_ANNUALLY, YEARLY, PRINCIPAL_MATURITY));
    static final Supplier<Set<AmendableFieldType>> cyclicAmendableTradeMessageFieldTypeProvider = new CyclicAmendableTradeMessageFieldProvider(getListOfAmendableTradeMessageFieldTypes());
    static final Supplier<TradeLegType> cyclicAmendableMmLegProvider = new CyclicAmendableTradeLegTypeProvider(List.of(MM_PRINCIPAL, MM_MATURITY, MM_MATURITY, MM_MATURITY, MM_MATURITY, MM_PRINCIPAL, MM_MATURITY));

    static final class CyclicRateTypeProvider extends AbstractCyclicDataProvider<RateType> {
        CyclicRateTypeProvider(List<RateType> dataList) {
            super(dataList);
        }
    }

    static final class CyclicInterestPayoutFrequencyProvider extends AbstractCyclicDataProvider<InterestPayoutFrequency> {
        CyclicInterestPayoutFrequencyProvider(List<InterestPayoutFrequency> dataList) {
            super(dataList);
        }
    }

    static List<Set<AmendableFieldType>> getListOfAmendableTradeMessageFieldTypes() {
        return List.of(
                Set.of(COUNTERPARTY_CODE),
                Set.of(AMOUNT),
                Set.of(VALUE_DATE, AMOUNT),
                Set.of(COUNTERPARTY_CODE, AMOUNT),
                Set.of(VALUE_DATE, AMOUNT, COUNTERPARTY_CODE)
        );
    }

    static class CyclicAmendableTradeMessageFieldProvider extends AbstractCyclicDataProvider<Set<AmendableFieldType>> {
        public CyclicAmendableTradeMessageFieldProvider(List<Set<AmendableFieldType>> fields) {
            super(fields);
        }
    }

    static class CyclicAmendableTradeLegTypeProvider extends AbstractCyclicDataProvider<TradeLegType> {
        public CyclicAmendableTradeLegTypeProvider(List<TradeLegType> mmLegs) {
            super(mmLegs);
        }
    }
}
