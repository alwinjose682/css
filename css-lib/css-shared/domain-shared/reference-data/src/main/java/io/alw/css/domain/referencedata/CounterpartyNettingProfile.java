package io.alw.css.domain.referencedata;

import io.alw.css.domain.cashflow.TradeType;
import io.alw.css.domain.common.CssNettingType;
import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@RecordBuilder
public record CounterpartyNettingProfile(
        long nettingProfileID,
        int nettingProfileVersion,
        @NotNull String counterpartyCode,
        int counterpartyVersion,
        @NotNull TradeType product, // Product and TradeType are treated as same for simplicity
        @NotNull CssNettingType nettingType,
        boolean netByParentCounterpartyCode,
        boolean netForAnyEntity,
        @Nullable String entityCode, // if 'netForAnyEntity' is 'N'/false, then 'entityCode' must have a value
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
