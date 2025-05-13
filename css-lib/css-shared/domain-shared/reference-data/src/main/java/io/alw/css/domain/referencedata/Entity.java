package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@RecordBuilder
public record Entity(
        @NotNull String entityCode,
        int entityVersion,
        String entityName,
        String currCode,
        String countryCode,
        String countryName,
        String bicCode,
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
