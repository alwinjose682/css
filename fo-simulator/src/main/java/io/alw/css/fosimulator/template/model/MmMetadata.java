package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.MmTradeType;
import io.alw.css.domain.cashflow.RateType;

public sealed interface MmMetadata permits MmCashLeg {
    MmTradeType mmType();
    RateType rateType();
    InterestPayoutFrequency ipFrequency();
    InterestBasis interestBasis();
}
