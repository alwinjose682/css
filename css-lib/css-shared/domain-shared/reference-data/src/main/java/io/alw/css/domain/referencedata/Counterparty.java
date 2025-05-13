package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;


@RecordBuilder
public record Counterparty(
        @NotBlank String counterpartyCode,
        int counterpartyVersion,
        String counterpartyName,
        boolean parent,
        boolean internal,
        @Nullable String parentCounterpartyCode,
        String counterpartyType,
        String bicCode,
        @Nullable String entityCode, // non-null only if `internal`=true
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String region,
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
