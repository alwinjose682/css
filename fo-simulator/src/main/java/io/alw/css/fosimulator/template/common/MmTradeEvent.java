package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.TradeEventType;

import java.util.HashSet;
import java.util.Set;

/// The name of Trade Events specified in this file corresponds with [TradeEventType] except haircut event
public sealed interface MmTradeEvent extends TradeEvent {
    record INTEREST_ACTION  (int hi, int lo) implements MmTradeEvent{}
    record ROLL             (int hi, int lo) implements MmTradeEvent{}
    record HAIRCUT          (int hi, int lo) implements MmTradeEvent{}
    record TERMINATE        (int hi, int lo) implements MmTradeEvent{}
}
