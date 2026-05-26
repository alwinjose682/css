package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.MmLeg;
import io.alw.css.domain.cashflow.MmType;
import io.alw.css.domain.cashflow.RateType;

public sealed interface MmMetadata permits MmCashLeg {
    MmType mmType();
    MmLeg mmLeg();
    RateType rateType();
    InterestPayoutFrequency ipFrequency();
    InterestBasis interestBasis();
}
