package io.alw.css.domain.cashflow;

import java.util.List;

import static io.alw.css.domain.cashflow.TradeEventAction.*;

/**
 * The event type for each trade version. Can have multiple versions depending on the action taken on the event
 */
public enum TradeEventType {
    // A very limited number of trade events for different trade types

    // Common for all trades
    /// First Version
    /// / Only EventAction.ADD is valid
    NEW_TRADE(List.of(ADD)),
    REBOOK(List.of(ADD)),

    /// All EventAction, including REMOVE, are valid for AMEND and CORRECTION.
    /// Once REMOVEd the CF is cancelled in CSS, but trade remains live in FO, but remains Un-Authorized.
    /// Later, when Authorized, via one of the TradeEventType, a new cashflow will be generated and send to CSS.
    AMEND(List.of(ADD,MODIFY,REMOVE)),
    CORRECTION(List.of(ADD,MODIFY,REMOVE)), // Correction is required only for special cases. For example, when the value of the security decreases 10% or more. Not applicable for all trades.
    /// EventAction.REMOVE is not valid for BOOK_MOVE.
    BOOK_MOVE(List.of(ADD,MODIFY)),

    /// Full trade cancellation. Only EventAction.ADD is valid
    CANCEL(List.of(ADD)),


    // Option
    /// KNOCK_IN event is not relevant from a settlement perspective
    /// Only EventAction.ADD is valid for EXERCISE, KNOCK_OUT and EXPIRE. Trade itself needs to be cancelled or amended in FO if these EventType needs to be undone
    EXERCISE(List.of(ADD)),
    KNOCK_OUT(List.of(ADD)),
    EXPIRE(List.of(ADD)),

    // MM, Repo
    /// Only EventAction.ADD is valid.
    ROLL(List.of(ADD)),
    /// Terminate is not trade cancellation. Ex, Early maturity of MM Term deal
    TERMINATE(List.of(ADD)),

    // NDF
    /// Only EventAction.ADD is valid.
    FIX(List.of(ADD)),
    UN_FIX(List.of(ADD)),

    // FixedIncome, like Bond
    /// Only EventAction.ADD is valid.
    INTEREST_ACTION(List.of(ADD)),
    MATURE(List.of(ADD));

    private final List<TradeEventAction> actions;

    TradeEventType(List<TradeEventAction> actions) {
        this.actions = actions;
    }

    public List<TradeEventAction> actions() {
        return actions;
    }
}
