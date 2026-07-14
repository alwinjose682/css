package io.alw.css.tradepublisher.trade.model;

import io.alw.css.tradepublisher.IdProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GeneratorInitialValues(
        @NotNull LocalDate valueDate,
        @Positive long tradeId,
        @Positive long matchStatusEventId
) {
    public static GeneratorInitialValues defaultValues() {
        return new GeneratorInitialValues(LocalDate.now(), IdProvider.defaultInitialTradeId, IdProvider.defaultConfMatchEventId);
    }
}
