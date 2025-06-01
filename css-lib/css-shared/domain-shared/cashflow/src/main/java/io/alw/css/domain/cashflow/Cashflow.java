package io.alw.css.domain.cashflow;

import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.common.PaymentSuppressionCategory;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/// Note on secondaryLedgerAccount/sla:
/// sla is NOT unique for a nostroID. sla can be changed by amending the nostro.
/// So, when default selection of nostroID is overridden due to the presence of this sla mapping, it must be verified that the nostroId selected based on sla belongs to the same entityCode and currCode defined by this mapping
///
/// Why not simply map counterparty directly to a nostroID instead of the sla?
/// The back office application I used to work for did not have such a simple mapping. Dont know about the complexities of designing and managing a reference data system
@RecordBuilder
public record Cashflow(
        // CSS Cashflow Version Data
        /*set*/long cashflowID,
        /*set*/int cashflowVersion,
        /*set*/boolean latest, // the field 'latest' is intended to be used only by CSS Services that synchronizes Cashflow processing by acquiring a lock
        /*set*/RevisionType revisionType,

        // Fo Cashflow Version Data
        /*set*/long foCashflowID,
        /*set*/int foCashflowVersion,
        /*set*/long tradeID,
        /*set*/int tradeVersion,

        // Trade and Cashflow Data
        /*set*/TradeType tradeType,
        /*set*/String bookCode,
        /*set*/String counterBookCode, // Can be null if not an internal trade
        /*set*/String secondaryLedgerAccount,
        /*set*/TransactionType transactionType,
        /*set*/BigDecimal rate,
        /*set*/@NotNull LocalDate valueDate,
        // tradeLinks Can be null. Only first version and the version for which there is a change or new tradeLink must have a value, other versions can have this field null
        /*set*/@Valid List<TradeLink> tradeLinks,

        // ObligationData
        /*set*/@NotBlank String entityCode,
        /*set*/@NotBlank String counterpartyCode,
        /*set*/@NotNull BigDecimal amount,
        /*set*/@NotBlank @Size(min = 3, max = 3) String currCode,

        // EnrichmentData
        /*--set--*/boolean internal, // interBook, interBranch and interCompany are categorized as internal. Payment should not be generated for interBook CF
        String nostroID,
        String ssiID, // The counterparty's SSI. If an interBook trade and hence no real ssiID, then the dummy ssiID for interBook will be used
        /*--set--*/@NotNull PaymentSuppressionCategory paymentSuppressionCategory,

        // Cashflow Entry Audit
        /*set*/InputBy inputBy, // indicates inputted by system(SYSTEM) or by user(MAN)
        /*set*/String inputByUserID,
        /*set*/LocalDateTime inputDateTime
) {
}
