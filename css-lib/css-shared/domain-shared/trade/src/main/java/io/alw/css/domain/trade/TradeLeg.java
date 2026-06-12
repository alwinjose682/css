package io.alw.css.domain.trade;

import io.alw.css.domain.common.PayOrReceive;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeLeg {
    private final long tradeLegId;
    private final int tradeLegVersion;
    private final TradeLegType tradeLegType;
    private final @NotNull BigDecimal rate;
    private final @NotNull LocalDate valueDate;
    private final @NotNull PayOrReceive payOrReceive;
    private final @NotNull BigDecimal amount;
    private final @NotBlank
    @Size(min = 3, max = 3) String currCode;

    public TradeLeg(
            long tradeLegId,
            int tradeLegVersion,
            TradeLegType tradeLegType,
            TradeEventType tradeEventType,
            TradeEventAction tradeEventAction,
            @NotNull BigDecimal rate,
            @NotNull LocalDate valueDate,
            @NotNull PayOrReceive payOrReceive,
            @NotNull BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currCode
    ) {
        this.tradeLegId = tradeLegId;
        this.tradeLegVersion = tradeLegVersion;
        this.tradeLegType = tradeLegType;
        this.tradeEventType = tradeEventType;
        this.tradeEventAction = tradeEventAction;
        this.rate = rate;
        this.valueDate = valueDate;
        this.payOrReceive = payOrReceive;
        this.amount = amount;
        this.currCode = currCode;
    }

    public long tradeLegId() {
        return tradeLegId;
    }

    public int tradeLegVersion() {
        return tradeLegVersion;
    }

    public TradeLegType tradeLegType() {
        return tradeLegType;
    }

    public TradeEventType tradeEventType() {
        return tradeEventType;
    }

    public TradeEventAction tradeEventAction() {
        return tradeEventAction;
    }

    public @NotNull BigDecimal rate() {
        return rate;
    }

    public @NotNull LocalDate valueDate() {
        return valueDate;
    }

    public @NotNull PayOrReceive payOrReceive() {
        return payOrReceive;
    }

    public @NotNull BigDecimal amount() {
        return amount;
    }

    public @NotBlank @Size(min = 3, max = 3) String currCode() {
        return currCode;
    }

}
