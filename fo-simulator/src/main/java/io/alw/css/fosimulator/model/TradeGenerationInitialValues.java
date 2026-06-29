package io.alw.css.fosimulator.model;

import io.alw.css.fosimulator.template.IdProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TradeGenerationInitialValues(
        @NotNull LocalDate valueDate,
        @Positive long tradeId
) {
    public static TradeGenerationInitialValues defaultValues() {
        return new TradeGenerationInitialValues(LocalDate.now(), IdProvider.defaultInitialTradeId);
    }
}
