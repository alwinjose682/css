package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.RateType;

public sealed interface MmMetadata permits MmCashLeg {
    RateType rateType();
    InterestPayoutFrequency ipFrequency();
    InterestBasis interestBasis();
}
