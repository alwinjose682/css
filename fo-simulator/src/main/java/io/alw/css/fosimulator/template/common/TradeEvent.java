package io.alw.css.fosimulator.template.common;

import java.util.HashSet;
import java.util.Set;
import io.alw.css.domain.cashflow.TradeEventType;

/// The name of Trade Events specified in this file matches with [TradeEventType] that applies to all trades
public sealed interface TradeEvent permits MmTradeEvent, TradeEvent.AMEND, TradeEvent.BOOK_MOVE, TradeEvent.CANCEL, TradeEvent.NEW_TRADE, TradeEvent.REBOOK {
    record NEW_TRADE() implements TradeEvent {}
    record AMEND    () implements TradeEvent {}
    record CANCEL   () implements TradeEvent {}
    record REBOOK   () implements TradeEvent {}
    record BOOK_MOVE() implements TradeEvent {}
    
    static Set<TradeEvent> commonTradeEvents(){
        var cte = new HashSet<TradeEvent>();
        cte.add(new NEW_TRADE());
        cte.add(new AMEND());
        cte.add(new CANCEL());
        cte.add(new REBOOK());
        cte.add(new BOOK_MOVE());

        return cte;
    }
}
