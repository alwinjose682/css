package io.alw.css.domain.cashflow;

import io.alw.css.domain.common.*;
import io.alw.css.domain.trade.TradeLegType;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/// Why not simply map counterparty directly to a nostroId instead of the sla?
/// The back office application I used to work for did not have such a simple mapping. Dont know about the complexities of designing and managing a reference data system
@RecordBuilder
public record Cashflow(
        // CSS Cashflow Version Data
        long cashflowId,
        int cashflowVersion,
        boolean latest, // the field 'latest' is intended to be used only by CSS Services that synchronizes Cashflow processing by acquiring a lock
        RevisionType revisionType,

        // Trade Id and Version
        long tradeId,
        int tradeVersion,
        long tradeLegId,
        int tradeLegVersion,

        // Trade and Cashflow Data
        TradeType tradeType,
        TradeLegType tradeLegType,
        String bookCode,
        String counterBookCode, // Can be null if not an internal trade
        TransactionType transactionType,
        BigDecimal rate,
        @NotNull LocalDate valueDate,

        // ObligationData
        @NotBlank String entityCode,
        @NotBlank String counterpartyCode,
        @NotNull BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currCode,

        // EnrichmentData
        boolean internal, // interBook, interBranch and interCompany are categorized as internal. Payment should not be generated for interBook CF
        String nostroId,
        String ssiId, // The counterparty's SSI. If an interBook trade and hence no real ssiId, then the dummy ssiId for interBook will be used
        @NotNull PaymentSuppressionCategory paymentSuppressionCategory,

        // Cashflow Entry Audit
        InputBy inputBy, // indicates inputted by system(SYSTEM) or by user(MAN)
        String inputByUserId,
        LocalDateTime inputDateTime
) {
}
