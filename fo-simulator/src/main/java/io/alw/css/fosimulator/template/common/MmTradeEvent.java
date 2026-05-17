package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.TradeEventType;

import java.util.HashSet;
import java.util.Set;

/// The name of Trade Events specified in this file matches with [TradeEventType] except haircut event
public sealed interface MmTradeEvent extends TradeEvent {
    record INTEREST_ACTION() implements MmTradeEvent{}
    record ROLL() implements MmTradeEvent{}
    record HAIRCUT() implements MmTradeEvent{}
    record TERMINATE() implements MmTradeEvent{}

//TODO: How about defining the percentage of chance to trigger a given event as a property of MmTradeEvent ?,
// like record HAIRCUT(int chancePercentage) implements MmTradeEvent{}
    static Set<TradeEvent> allTradeEvents(){
        var ate = new HashSet<TradeEvent>();
        ate.add(new INTEREST_ACTION());
        ate.add(new ROLL());
        ate.add(new HAIRCUT());
        ate.add(new TERMINATE());

        return ate;
    }
}
