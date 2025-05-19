package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.LocalTime;

///NOTE: The cutOffTime value of other currencies is determined relative to the Deutschland UTC time, ie UTC+1/CET
@RecordBuilder
public record Currency(
        @NotNull @Size(min = 3, max = 3) String currCode,
        String countryCode,
        @NotNull boolean pmFlag, // precious metal flag
        LocalTime cutOffTime,
        @NotNull boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
