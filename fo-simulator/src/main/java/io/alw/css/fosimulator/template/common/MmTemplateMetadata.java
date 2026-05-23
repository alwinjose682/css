package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.MmLeg;
import io.alw.css.domain.cashflow.RateType;

import java.math.BigDecimal;
import java.util.Map;

public sealed interface MmTemplateMetadata {

    MmLeg mmLeg();

    RateType rateType();

    InterestPayoutFrequency ipFrequency();

    InterestBasis interestBasis();

    MmTemplateMetadataHolder metadata();

    record Term(MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis, MmTemplateMetadataHolder metadata) implements MmTemplateMetadata {
        public Term(MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
            this(mmLeg, rateType, ipFrequency, interestBasis, new MmTemplateMetadataHolder());
        }
    }

    record Call(MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis, MmTemplateMetadataHolder metadata) implements MmTemplateMetadata {
        public Call(MmLeg mmLeg, RateType rateType, InterestPayoutFrequency ipFrequency, InterestBasis interestBasis) {
            this(mmLeg, rateType, ipFrequency, interestBasis, new MmTemplateMetadataHolder());
        }
    }

    final class MmTemplateMetadataHolder {
        private BigDecimal interestAmount;
//        private final Map<String,Object> otherMetaData;

        public void setInterestAmount(BigDecimal interestAmount) {
            this.interestAmount = interestAmount;
        }

        public BigDecimal interestAmount() {
            return interestAmount;
        }
    }
}
