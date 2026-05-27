package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.model.TradeEventActionPair;

import java.util.List;
import java.util.function.Consumer;

public record CashMessageAmendmentContext(
        List<AmendableFoCashMessageField> amendableFields,
        FoCashMessage msg,
        Consumer<FoCashMessage> callback,
        TradeEventActionPair tradeEventActionPair) {
}
