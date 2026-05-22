package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.MmLeg;
import io.alw.css.domain.cashflow.RateType;

public sealed interface MmTemplateType {

    MmLeg mmLeg();

    RateType rateType();

    InterestPayoutFrequency ipFrequency();

    InterestBasis interestBasis();

    record Term(MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) implements MmTemplateType {
    }

    record Call(MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) implements MmTemplateType {
    }
}
