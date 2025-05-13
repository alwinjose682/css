package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDateTime;

@RecordBuilder
public record Country(
        String countryCode,
        String countryName,
        String region,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
