package io.alw.css.domain.cashflow;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RecordBuilder
public record FoCashMessage(
        long cashflowID,
        int cashflowVersion,
        // TradeData
        long tradeID,
        int tradeVersion,
        @NotNull TradeType tradeType,
        @NotNull String bookCode,
        String counterBookCode, // Can be null if not an internal trade
        String secondaryLedgerAccount, // If null, the primary nostro will be selected for the given entity and curr code combination
        @NotNull TransactionType transactionType,
        @NotNull TradeEventType tradeEventType,
        @NotNull TradeEventAction tradeEventAction,
        @NotNull BigDecimal rate,
        @NotNull LocalDate valueDate,
        @Valid @NotNull List<TradeLink> tradeLinks,
// Can be null. Only first version and the version for which there is a change or new tradeLink must have a value, other versions can have this field null
        // ObligationData
        @NotNull String entityCode,
        @NotNull String counterpartyCode,
        @NotNull PayOrRecieve payOrRecieve,
        @NotNull BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currCode
) implements TestDataGeneratable {
}