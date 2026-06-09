package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.TradeEventType;

/// The name of Trade Events specified in this file corresponds with [TradeEventType] except haircut event
public sealed interface MmTradeEvent extends TradeEventTypeRecord {
    record INTEREST_ACTION  (TradeEventType standardTradeEventType) implements MmTradeEvent{}
    record ROLL             (TradeEventType standardTradeEventType) implements MmTradeEvent{}
    record HAIRCUT          (TradeEventType standardTradeEventType) implements MmTradeEvent{}
    record TERMINATE        (TradeEventType standardTradeEventType) implements MmTradeEvent{}
}
