package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.RateType;
import io.alw.css.fosimulator.template.model.AmendableFoCashMessageFieldType;
import io.alw.css.fosimulator.template.domain.CashLegType;
import io.alw.css.fosimulator.template.domain.InterestPayoutFrequency;
import io.alw.datagen.provider.AbstractCyclicDataProvider;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static io.alw.css.domain.cashflow.RateType.FIXED;
import static io.alw.css.domain.cashflow.RateType.FLOAT;
import static io.alw.css.fosimulator.template.domain.CashLegType.MM_MATURITY;
import static io.alw.css.fosimulator.template.domain.CashLegType.MM_PRINCIPAL;
import static io.alw.css.fosimulator.template.domain.InterestPayoutFrequency.*;
import static io.alw.css.fosimulator.template.model.AmendableFoCashMessageFieldType.*;

public final class MmTemplateConstants {
    static final double principalLegAmountOrigin = 100000;
    static final double principalLegAmountBound = 98500000;

    static final Supplier<RateType> cyclicRateTypeProvider = new CyclicRateTypeProvider(List.of(FIXED, FLOAT, FIXED, FLOAT));
    static final Supplier<InterestPayoutFrequency> cyclicIpFrequencyProvider = new CyclicInterestPayoutFrequencyProvider(List.of(DAY, MONTHLY, MONTHLY, MONTHLY, PRINCIPAL_MATURITY, MONTHLY, QUARTERLY, QUARTERLY, SEMI_ANNUALLY, YEARLY, PRINCIPAL_MATURITY));
    static final Supplier<Set<AmendableFoCashMessageFieldType>> cyclicAmendableFoCashMessageFieldTypeProvider = new CyclicAmendableFoCashMessageFieldProvider(getListOfAmendableCashMessageFieldTypes());
    static final Supplier<CashLegType> cyclicAmendableMmLegProvider = new CyclicAmendableCashLegTypeProvider(List.of(MM_PRINCIPAL, MM_MATURITY, MM_MATURITY, MM_MATURITY, MM_MATURITY, MM_PRINCIPAL, MM_MATURITY));

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

    static List<Set<AmendableFoCashMessageFieldType>> getListOfAmendableCashMessageFieldTypes() {
        return List.of(
                Set.of(COUNTERPARTY_CODE),
                Set.of(AMOUNT),
                Set.of(VALUE_DATE, AMOUNT),
                Set.of(COUNTERPARTY_CODE, AMOUNT),
                Set.of(VALUE_DATE, AMOUNT, COUNTERPARTY_CODE)
        );
    }

    static class CyclicAmendableFoCashMessageFieldProvider extends AbstractCyclicDataProvider<Set<AmendableFoCashMessageFieldType>> {
        public CyclicAmendableFoCashMessageFieldProvider(List<Set<AmendableFoCashMessageFieldType>> fields) {
            super(fields);
        }
    }

    static class CyclicAmendableCashLegTypeProvider extends AbstractCyclicDataProvider<CashLegType> {
        public CyclicAmendableCashLegTypeProvider(List<CashLegType> mmLegs) {
            super(mmLegs);
        }
    }
}
