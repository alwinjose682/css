package io.alw.css.tradeconsumer.processor.rule;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;

record TradeEventAndAction(TradeEventType event, TradeEventAction action) {
}
