package io.alw.css.domain.trade;

import io.alw.css.domain.common.*;
import io.alw.datagen.TestDataGeneratable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;


public class Trade implements TestDataGeneratable {
    private final long tradeID;
    private final int tradeVersion;
    private final @NotNull TradeType tradeType;
    private final String bookCode;
    private final String counterBookCode;
    private final TransactionType transactionType;
    private final @NotNull String entityCode;
    private final @NotNull String counterpartyCode;
    private final List<TradeLink> tradeLinks;
    private final Map<TradeLegType, TradeLeg> tradeLegs;
    private final TradeEventType tradeEventType;
    private final TradeEventAction tradeEventAction;

    public Trade(
            long tradeID,
            int tradeVersion,
            @NotNull TradeType tradeType,
            String bookCode,
            String counterBookCode, // Can be null if not an internal trade
            TransactionType transactionType,
            @NotNull String entityCode,
            @NotNull String counterpartyCode,
            List<TradeLink> tradeLinks,
            Map<TradeLegType, TradeLeg> tradeLegs
    ) {
        this.tradeID = tradeID;
        this.tradeVersion = tradeVersion;
        this.tradeType = tradeType;
        this.bookCode = bookCode;
        this.counterBookCode = counterBookCode;
        this.transactionType = transactionType;
        this.entityCode = entityCode;
        this.counterpartyCode = counterpartyCode;
        this.tradeLinks = tradeLinks;
        this.tradeLegs = tradeLegs;
    }

    public static MutableTradeBuilder builder() {
        return new MutableTradeBuilder();
    }

    public long tradeID() {
        return tradeID;
    }

    public int tradeVersion() {
        return tradeVersion;
    }

    public @NotNull TradeType tradeType() {
        return tradeType;
    }

    public String bookCode() {
        return bookCode;
    }

    public String counterBookCode() {
        return counterBookCode;
    }

    public TransactionType transactionType() {
        return transactionType;
    }

    public @NotNull String entityCode() {
        return entityCode;
    }

    public @NotNull String counterpartyCode() {
        return counterpartyCode;
    }

    public List<TradeLink> tradeLinks() {
        return tradeLinks;
    }

    public Map<TradeLegType, TradeLeg> tradeLegs() {
        return tradeLegs;
    }
}
