package io.alw.css.domain.trade;

import io.alw.css.domain.common.PayOrReceive;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


/*
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
) {
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
 */


public final class TradeLeg {
    private final long tradeLegId;
    private final int tradeLegVersion;
    private final TradeLegType tradeLegType;
    private final TradeEventType tradeEventType;
    private final TradeEventAction tradeEventAction;
    private final @NotNull String entityCode;
    private final String bookCode;
    private final String counterBookCode;
    private final @NotBlank String counterpartyCode;
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
            @NotNull String entityCode,
            String bookCode,
            String counterBookCode, // Can be null if not an internal trade
            @NotBlank String counterpartyCode,
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
        this.entityCode = entityCode;
        this.bookCode = bookCode;
        this.counterBookCode = counterBookCode;
        this.counterpartyCode = counterpartyCode;
        this.rate = rate;
        this.valueDate = valueDate;
        this.payOrReceive = payOrReceive;
        this.amount = amount;
        this.currCode = currCode;
    }

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

    public @NotNull String entityCode() {
        return entityCode;
    }

    public String bookCode() {
        return bookCode;
    }

    public String counterBookCode() {
        return counterBookCode;
    }

    public @NotBlank String counterpartyCode() {
        return counterpartyCode;
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

    @Override
    public String toString() {
        return "TradeLeg[" +
                "tradeLegId=" + tradeLegId + ", " +
                "tradeLegVersion=" + tradeLegVersion + ", " +
                "tradeLegType=" + tradeLegType + ", " +
                "tradeEventType=" + tradeEventType + ", " +
                "tradeEventAction=" + tradeEventAction + ", " +
                "entityCode=" + entityCode + ", " +
                "bookCode=" + bookCode + ", " +
                "counterBookCode=" + counterBookCode + ", " +
                "counterpartyCode=" + counterpartyCode + ", " +
                "rate=" + rate + ", " +
                "valueDate=" + valueDate + ", " +
                "payOrReceive=" + payOrReceive + ", " +
                "amount=" + amount + ", " +
                "currCode=" + currCode + ']';
    }

}



