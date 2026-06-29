package io.alw.css.tradepublisher.model;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;

public record TradeEventActionPair(TradeEventType event, TradeEventAction action) {
}
