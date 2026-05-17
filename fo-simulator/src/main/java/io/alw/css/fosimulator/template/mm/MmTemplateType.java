package io.alw.css.fosimulator.template.mm;

import io.alw.css.domain.cashflow.RateType;
import io.alw.css.fosimulator.template.common.InterestBasis;
import io.alw.css.fosimulator.template.common.InterestPayoutFrequency;

public interface MmTemplateType {

    RateType rateType();

    InterestPayoutFrequency ipFrequency();

    InterestBasis interestBasis();

    record Term(RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) implements MmTemplateType {
    }

    record Call(RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) implements MmTemplateType {
    }
}
