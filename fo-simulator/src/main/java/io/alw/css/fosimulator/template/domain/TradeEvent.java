package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.TradeEventType;

/// The name of Trade Events specified in this file corresponds with [TradeEventType] that applies to all trades
public sealed interface TradeEvent
        permits MmTradeEvent,
        TradeEvent.AMEND, TradeEvent.BOOK_MOVE, TradeEvent.CANCEL, TradeEvent.NEW_TRADE, TradeEvent.REBOOK {
    int hi();
    int lo();

    record NEW_TRADE(int hi, int lo, TradeEventType standardTradeEventType) implements TradeEvent {}
    record AMEND    (int hi, int lo, TradeEventType standardTradeEventType) implements TradeEvent {}
    record CANCEL   (int hi, int lo, TradeEventType standardTradeEventType) implements TradeEvent {}
    record REBOOK   (int hi, int lo, TradeEventType standardTradeEventType) implements TradeEvent {}
    record BOOK_MOVE(int hi, int lo, TradeEventType standardTradeEventType) implements TradeEvent {}
}
