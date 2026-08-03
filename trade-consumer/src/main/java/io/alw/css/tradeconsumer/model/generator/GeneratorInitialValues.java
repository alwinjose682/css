package io.alw.css.tradeconsumer.model.generator;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GeneratorInitialValues(
        @NotNull LocalDate valueDate,
        @Positive long tradeId,
        @Positive long matchStatusEventId
) {
}
