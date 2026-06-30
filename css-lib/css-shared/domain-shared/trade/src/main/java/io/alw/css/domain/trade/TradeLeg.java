package io.alw.css.domain.trade;

import io.alw.css.domain.common.PayOrReceive;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@RecordBuilder
public record TradeLeg(
        long tradeLegId,
        int tradeLegVersion,
        TradeLegType tradeLegType,
        TradeEventType tradeEventType,
        TradeEventAction tradeEventAction,
        @NotNull String entityCode,
        String bookCode,
        String counterBookCode, // Can be null if not an internal trade
        @NotBlank String counterpartyCode,
        @NotNull BigDecimal rate,
        @NotNull LocalDate valueDate,
        @NotNull PayOrReceive payOrReceive,
        @NotNull BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currCode
) implements TradeDetail {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TradeLeg tradeLeg = (TradeLeg) o;
        return tradeLegId == tradeLeg.tradeLegId && tradeLegVersion == tradeLeg.tradeLegVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeLegId, tradeLegVersion);
    }
}
