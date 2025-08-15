package io.alw.css.fosimulator.model;

import io.alw.css.fosimulator.definition.IdProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CashflowGeneratorInitialValues(
        @NotNull LocalDate valueDate,
        @Positive long tradeId,
        @Positive long foCashflowId
) {
    public static CashflowGeneratorInitialValues defaultValues() {
        return new CashflowGeneratorInitialValues(LocalDate.now(), IdProvider.defaultInitialTradeId, IdProvider.defaultInitialFoCfId);
    }
}
