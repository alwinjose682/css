package io.alw.css.fosimulator.model;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;

public record TradeEventActionPair(TradeEventType event, TradeEventAction action) {
}
