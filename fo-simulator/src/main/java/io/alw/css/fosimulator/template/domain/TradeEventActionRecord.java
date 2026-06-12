package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.TradeEventAction;

/// The name of Trade Events specified in this file corresponds with [TradeEventAction] that applies to all trades
///
/// NOTE: This class is purely for convenience’s sake to use switch expression rather than if-else-ladder for expressing the logic of -> CashMessageAmendmentTemplate#getNextEventActionPair(TradeEventType, TradeEventAction).
///
/// It does not seem good to convert the [TradeEventAction] enum to record for other uses
public sealed interface TradeEventActionRecord {
    TradeEventAction standardTradeEventAction();

    record ADD(TradeEventAction standardTradeEventAction) implements TradeEventActionRecord {}
    record MODIFY(TradeEventAction standardTradeEventAction) implements TradeEventActionRecord {}
    record REMOVE(TradeEventAction standardTradeEventAction) implements TradeEventActionRecord {}

    ADD add  = new ADD(TradeEventAction.ADD);
    MODIFY modify  = new MODIFY(TradeEventAction.MODIFY);
    REMOVE remove  = new REMOVE(TradeEventAction.REMOVE);

    static TradeEventActionRecord getCorrespondingTradeEventAction(TradeEventAction standardAction) {
        return switch (standardAction){
            case ADD -> add;
            case MODIFY -> modify;
            case REMOVE -> remove;
        };
    }
}
