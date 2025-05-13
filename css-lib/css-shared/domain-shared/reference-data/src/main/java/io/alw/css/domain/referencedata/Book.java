package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@RecordBuilder
public record Book(
        @NotBlank String bookCode,
        int bookVersion,
        String bookName,
        String entityCode,
        int entityVersion,
        String productLine, // I intend to simply use the TradeType as productLine name
        String division,
        String superDivision,
        String cluster,
        String subCluster,
        String tradeGroup,
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
