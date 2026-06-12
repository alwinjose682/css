package io.alw.css.domain.trade;

import io.alw.css.domain.common.PayOrReceive;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeLink;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RecordBuilder
public record TradeLeg(
        long tradeLegId,
        int tradeLegVersion,
        TradeEventType tradeEventType,
        TradeEventAction tradeEventAction,
        @NotNull BigDecimal rate,
        @NotNull LocalDate valueDate,
        List<TradeLink> tradeLinks,
        @NotNull PayOrReceive payOrReceive,
        @NotNull BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currCode
) {
}
