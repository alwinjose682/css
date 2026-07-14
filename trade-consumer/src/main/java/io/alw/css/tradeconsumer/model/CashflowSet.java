package io.alw.css.tradeconsumer.model;

import io.alw.css.domain.cashflow.Cashflow;

/// TradeConfirmationService require the previous cashflow to get the previous confMatchRequestId.
/// There are two choices:
/// 1) either query database for confMatchRequestId for each previous cashflow (but the prev cashflow id are needed to make for querying)
/// OR 2) store the confMatchRequestId with cashflow record and pass it in this object to the TradeConfirmationService
///
/// TODO: When other parts are improved to not fetch the whole cashflow record from database, this can also be changed accordingly
public sealed interface CashflowSet {
    Cashflow primaryCashflow();

    record InitialVersion(Cashflow cashflow) implements CashflowSet {
        @Override
        public Cashflow primaryCashflow() {
            return cashflow;
        }
    }

    record SubsequentVersion(Cashflow amendCashflow, Cashflow prevCashflow, Cashflow revCashflow) implements CashflowSet {
        @Override
        public Cashflow primaryCashflow() {
            return amendCashflow;
        }
    }

    record CancelledVersion(Cashflow canCashflow, Cashflow prevCashflow) implements CashflowSet {
        @Override
        public Cashflow primaryCashflow() {
            return canCashflow;
        }
    }
}
