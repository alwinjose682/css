package io.alw.css.tradeconsumer.cashflow.model;

import io.alw.css.domain.cashflow.Cashflow;

/// PreviousCashflow is the last processed cashflow of the given trade & tradeLeg combination
public sealed interface PreviousCashflowCheckOutcome {
    SameAsPrevCashflow SAME_AS_PREVIOUS_CASHFLOW = new SameAsPrevCashflow();
    InitialVersion INITIAL_VERSION = new InitialVersion();

    record SameAsPrevCashflow() implements PreviousCashflowCheckOutcome {
    }

    record InitialVersion() implements PreviousCashflowCheckOutcome {
    }

    record SubsequentVersion(Cashflow lastProcessedCashflow) implements PreviousCashflowCheckOutcome {
    }

    record PrevCashflowIsCancelled() implements PreviousCashflowCheckOutcome {
    }
}
