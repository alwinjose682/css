package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.model.TradeEventActionPair;

import java.util.List;
import java.util.function.Function;

/// Note: The RootAmendedCashMessage here is the cashMessage selected for amendment that has caused amendment for other related cashMessages
public final class RootAmendedCashMessageContext {
    private final List<AmendableFoCashMessageField> amendableFields;
    private final TradeEventActionPair tradeEventActionPair;
    private Ids firstAmendedCashMessageIds;

    public RootAmendedCashMessageContext(List<AmendableFoCashMessageField> amendableFields, TradeEventActionPair tradeEventActionPair) {
        this.amendableFields = amendableFields;
        this.tradeEventActionPair = tradeEventActionPair;
    }

    public Ids computeFirstAmendedCashMessageIdsIfAbsent(FoCashMessage amendmentSubject, Function<FoCashMessage, Ids> computeFunc) {
        if (firstAmendedCashMessageIds == null) {
            firstAmendedCashMessageIds = computeFunc.apply(amendmentSubject);
            return firstAmendedCashMessageIds;
        } else {
            return firstAmendedCashMessageIds;
        }
    }

    public List<AmendableFoCashMessageField> amendableFields() {
        return amendableFields;
    }

    public TradeEventActionPair tradeEventActionPair() {
        return tradeEventActionPair;
    }
}
