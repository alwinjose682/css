package io.alw.css.tradepublisher.trade.template.domain;

import io.alw.css.domain.common.TradeEventType;

/// The name of Trade Events specified in this file corresponds with [TradeEventType] that are common to all trades
///
/// NOTE: This class is purely for convenience’s sake to use switch expression rather than if-else-ladder for expressing the logic of -> TradeAmendmentTemplate#getNextEventActionPair(TradeEventType, TradeEventAction).
///
/// It does not seem good to convert the [TradeEventType] enum to record for other uses
public sealed interface TradeEventTypeRecord
        permits MmTradeEvent,
        TradeEventTypeRecord.AMEND, TradeEventTypeRecord.CANCEL, TradeEventTypeRecord.NEW_TRADE, TradeEventTypeRecord.REBOOK {
    TradeEventType standardTradeEventType();

    record NEW_TRADE(TradeEventType standardTradeEventType) implements TradeEventTypeRecord {}
    record AMEND    (TradeEventType standardTradeEventType) implements TradeEventTypeRecord {}
    record REBOOK   (TradeEventType standardTradeEventType) implements TradeEventTypeRecord {}
    record CANCEL   (TradeEventType standardTradeEventType) implements TradeEventTypeRecord {}
//    record BOOK_MOVE(BOOK_MOVE bookMove, TradeEventType standardTradeEventType) implements TradeEvent {} -> not implemented yet

    NEW_TRADE newTrade = new NEW_TRADE(TradeEventType.NEW_TRADE);
    AMEND amend = new AMEND(TradeEventType.AMEND);
    REBOOK rebook = new REBOOK(TradeEventType.REBOOK);
    CANCEL cancel = new CANCEL(TradeEventType.CANCEL);

    static TradeEventTypeRecord getCorrespondingTradeEventRecord(TradeEventType standardEvent) {
        return switch (standardEvent){
            case NEW_TRADE -> newTrade;
            case REBOOK -> rebook;
            case AMEND -> amend;
            case CANCEL -> cancel;
            case MATURE, INTEREST_ACTION, COUPON, UN_FIX, FIX, TERMINATE, ROLL, EXPIRE, KNOCK_OUT, EXERCISE, BOOK_MOVE, CORRECTION -> throw new IllegalStateException("Mapping from TradeEventRecord to TradeEventType does not exist for: " + standardEvent);
        };
    }
}
