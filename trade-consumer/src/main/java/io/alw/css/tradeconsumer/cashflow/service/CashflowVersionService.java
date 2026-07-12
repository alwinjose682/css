package io.alw.css.tradeconsumer.cashflow.service;

import io.alw.css.dbshared.tx.TXRO;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.cashflow.CashflowConstants;
import io.alw.css.domain.common.*;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.tradeconsumer.cashflow.model.PreviousCashflowCheckOutcome;
import io.alw.css.tradeconsumer.cashflow.repository.CashflowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.*;

/// Does the following:
/// - Determines whether the new cashflow is firstVersion, nonFirstVersion or alreadyProcessed
/// - computes the CSS [RevisionType] based on FO's [TradeEventAction], [TradeEventType] and [TradeType]
/// - sets the appropriate [Cashflow#latest] value
/// - creates NEW, COR+Offsetting, CAN cashflows according to above outcome.
/// - assigns new cashflowId and version for the cashflow to be processed
/// - Finally, creates the [Cashflow]
///
/// Q: In an amendment(COR) or cancellation(CAN) scenario, how to identify which cashflow the COR or CAN cashflow offsets?
/// - There is no extra identifier to determine this.
/// It is ensured that the CFs are processed sequentially in order to guarantee that an N+2 version of a CF does not offset a live Nth version.
/// This is ensured by verifying the sequential order of [io.alw.css.domain.trade.Trade#tradeVersion] and [TradeLeg#tradeLegVersion()]
public class CashflowVersionService {
    private static final Logger log = LoggerFactory.getLogger(CashflowVersionService.class);

    private final CashflowStore cashflowStore;
    private final TXRW txrw;
    private final TXRO txro;

    public CashflowVersionService(CashflowStore cashflowStore, TXRW txrw, TXRO txro) {
        this.cashflowStore = cashflowStore;
        this.txrw = txrw;
        this.txro = txro;
    }

    /// Checks if the cashflow is [PreviousCashflowCheckOutcome.InitialVersion] or [PreviousCashflowCheckOutcome.SubsequentVersion] or [PreviousCashflowCheckOutcome.SameAsPrevCashflow]
    ///
    /// This is determined based on the last processed cashflow. Last processed cashflow(lpcf) exists as a result of one of the below:
    /// 1. A COR or CAN from FO
    /// 2. A COR or CAN by CSS User
    ///
    /// If last processed cashflow is cancelled, then no further amendment should be allowed.
    ///
    /// @return CFProcessedCheckOutcome
//    @Transactional(readOnly = true)
    public PreviousCashflowCheckOutcome checkAgainstLastProcessedCashflow(long tradeId, int tradeVersion, long tradeLegId, int tradeLegVersion) {
//        final Cashflow lastProcessedCashflow = cashflowStore.getLastProcessedCashflow(foCashflowID);
        final Cashflow lastProcessedCashflow = txro.execute(_ -> cashflowStore.getLastProcessedCashflow(tradeId, tradeLegId));

        if (lastProcessedCashflow == null) { /* if new cashflow */
            return PreviousCashflowCheckOutcome.INITIAL_VERSION;
        } else { /* if not a new cashflow */
            if (isCashflowAlreadyProcessed(tradeId, tradeVersion, tradeLegId, tradeLegVersion, lastProcessedCashflow)) {
                return PreviousCashflowCheckOutcome.SAME_AS_PREVIOUS_CASHFLOW;
            } else {
                if (isCashflowCancelled(lastProcessedCashflow)) {
                    return new PreviousCashflowCheckOutcome.PrevCashflowIsCancelled();
                }
                return new PreviousCashflowCheckOutcome.SubsequentVersion(lastProcessedCashflow);
            }
        }
    }

    /// Creates NEW cashflow. A NEW cashflow is possible only once, for the first version. Once a cashflow is cancelled, a new cashflowId needs to be used by FO for the same trade.
    /// Below values are explicitly set
    /// - cashflowId = new cashflowId
    /// - cashflowVersion = [CashflowConstants#CASHFLOW_FIRST_VERSION]
    /// - latest = true
    ///
    /// @implNote This method calls a dao method that does select on a DB sequence. Hence this method must be called in RW transaction
    // TODO: When transaction is readOnly for JpaTransactionManager, does spring cause libs to acquire a RO physical connection or just optimizes JPA dirty checking etc? AskVlad. But, Of course this depends on whether an eager physical connection fetch is made due to spring's settings
//    @Transactional
    public Cashflow createInitialVersionCashflow(CashflowBuilder bdr) {
        int tradeVersion = bdr.tradeVersion();
        int tradeLegVersion = bdr.tradeLegVersion();
        RevisionType revisionType = bdr.revisionType();
        if (tradeVersion != CashflowConstants.TRADE_FIRST_VERSION || tradeLegVersion != CashflowConstants.TRADE_FIRST_VERSION) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Trade and TradeLeg do no correspond to a version_1 cashflow.", new ExceptionSubCategory(NOT_FIRST_VERSION, null));
        } else if (revisionType != RevisionType.NEW) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Incorrect revisionType determination. RevisionType NEW is expected for version_1 cashflow. Computed RevisionType is: " + revisionType, new ExceptionSubCategory(INCORRECT_CF_REVISION_TYPE, null));
        }

//        long cashflowId = cashflowStore.getNewCashflowID();
        long cashflowID = txrw.execute(_ -> cashflowStore.getNewCashflowID());
        int cashflowVersion = CashflowConstants.CASHFLOW_FIRST_VERSION;
        Cashflow cashflow = bdr
                .cashflowId(cashflowID)
                .cashflowVersion(cashflowVersion)
                .latest(true)
                .build();

        return cashflow;
    }

    /// Creates COR+CAN cashflows or one CAN cashflow depending on [CashflowBuilder#revisionType]
    public List<Cashflow> createSubsequentVersion(Cashflow previousCashflow, CashflowBuilder bdr) {
        int tradeVersion = bdr.tradeVersion();
        int tradeLegVersion = bdr.tradeLegVersion();
        if (tradeVersion <= CashflowConstants.TRADE_FIRST_VERSION && tradeLegVersion <= CashflowConstants.TRADE_FIRST_VERSION) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Invalid attempt to create a subsequent cashflow version when both trade and tradeLeg are at initial version", new ExceptionSubCategory(INVALID_SUBSEQUENT_VERSION_CF_CREATION_ATTEMPT, null));
        }

        RevisionType revisionType = bdr.revisionType();
        return switch (revisionType) {
            case NEW -> throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Incorrect RevisionType determination as NEW", new ExceptionSubCategory(INCORRECT_CF_REVISION_TYPE, null));
            case COR -> createAmendmentAndOffsettingReversal(previousCashflow, bdr);
            case CAN -> {
                Cashflow cancelCashflow = createCancellation(previousCashflow, bdr);
                yield List.of(cancelCashflow);
            }
            case REV ->
                    throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("The cashflow fetched as the last processed cashflow for this tradeId-TradeLeg is a cashflow with RevisionType: REV", new ExceptionSubCategory(INCORRECT_PREV_CF_REVISION_TYPE, previousCashflow));
        };
    }

    private boolean isCashflowAlreadyProcessed(long tradeId, int tradeVersion, long tradeLegId, int tradeLegVersion, Cashflow lastProcessedCashflow) {
        long lpcfTradeId = lastProcessedCashflow.tradeId();
        int lpcfTradeVersion = lastProcessedCashflow.tradeVersion();
        long lpcfTradeLegId = lastProcessedCashflow.tradeLegId();
        int lpcfTradeLegVersion = lastProcessedCashflow.tradeLegVersion();

        if (lpcfTradeId == tradeId && tradeLegId == lpcfTradeLegId) {
            if (lpcfTradeVersion == tradeVersion && tradeLegVersion == lpcfTradeLegVersion) {
                return true;
            } else {
                return false;
            }
        } else {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("TradeId and/or TradeLegId do not match with that of the last processed processed cashflow", new ExceptionSubCategory(TRADE_ID_LEG_ID_MISMATCH, null));
        }
    }

    private boolean isCashflowCancelled(Cashflow cashflow) {
        return cashflow.revisionType() == RevisionType.CAN;
    }

    private Cashflow createCancellation(Cashflow previousCashflow, CashflowBuilder currentCashflowBdr) {
        if (previousCashflow.tradeId() != currentCashflowBdr.tradeId() || previousCashflow.tradeLegId() != currentCashflowBdr.tradeLegId()) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("TradeId and/or TradeLegId do not match for the previous cashflow and the current cashflow being processed", new ExceptionSubCategory(TRADE_ID_LEG_ID_MISMATCH, null));
        }

        BigDecimal prevCashflowAmount = previousCashflow.amount();
        Cashflow cashflow = CashflowBuilder.builder(previousCashflow)
                .revisionType(RevisionType.CAN)
                .cashflowVersion(previousCashflow.cashflowVersion() + 1)
                .tradeVersion(currentCashflowBdr.tradeVersion())
                .tradeLegVersion(currentCashflowBdr.tradeLegVersion())
                .amount(prevCashflowAmount.negate())
                .latest(true)
                .inputDateTime(currentCashflowBdr.inputDateTime())
                .build();

        log.debug("Created cancellation cashflow. CashflowID-Ver: {}-{}", cashflow.cashflowId(), cashflow.cashflowVersion());
        return cashflow;
    }

    private List<Cashflow> createAmendmentAndOffsettingReversal(Cashflow previousCashflow, CashflowBuilder cashflowBuilder) {
        Cashflow offsetCashflow = createOffsettingReversalCashflow(previousCashflow, cashflowBuilder);
        Cashflow amendCashflow = createAmendCashflow(previousCashflow, cashflowBuilder, offsetCashflow);
        log.debug("Created amendment cashflow and corresponding offset. CashflowID-Ver: {}-{}", amendCashflow.cashflowId(), amendCashflow.cashflowVersion());
        return List.of(offsetCashflow, amendCashflow);
    }

    /// Creates amended cashflow with confirmationStatus as NOT_CONFIRMED
    private Cashflow createAmendCashflow(Cashflow previousCashflow, CashflowBuilder cashflowBuilder, Cashflow offsetCashflow) {
        return cashflowBuilder
                .cashflowId(previousCashflow.cashflowId())
                .cashflowVersion(offsetCashflow.cashflowVersion() + 1)
                .confirmationStatus(CashflowConfirmationStatus.NOT_CONFIRMED)
                .latest(true)
                .build();
    }

    /// Creates a reversal cashflow to offset the last processed cashflow.
    /// confirmationStatus will remain same as that of the cashflow being reversed
    private Cashflow createOffsettingReversalCashflow(Cashflow previousCashflow, CashflowBuilder currCashflowBuilder) {
        BigDecimal prevCashflowAmount = previousCashflow.amount();
        return CashflowBuilder.builder(previousCashflow)
                .revisionType(RevisionType.REV)
                .cashflowVersion(previousCashflow.cashflowVersion() + 1)
                .amount(prevCashflowAmount.negate())
                .latest(false)
                .inputDateTime(currCashflowBuilder.inputDateTime())
                .build();
    }
}
