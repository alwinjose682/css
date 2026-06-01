package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.model.CashLegType;
import io.alw.css.fosimulator.model.TradeEventActionPair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/// Note: The `primaryAmendmentSubject` is the cashMessage selected for amendment that has caused amendment for other dependent cashMessages
public final class CashMessageAmendmentContext {
    private final List<AmendableFoCashMessageField> amendableFields;
    private final TradeEventActionPair tradeEventActionPair;
    private final AmendmentSubjectContext primaryAmendmentSubjectContext;
    private final List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts;
    private Ids primaryAmendmentSubjectUpdatedIds;

    public CashMessageAmendmentContext(List<AmendableFoCashMessageField> amendableFields, TradeEventActionPair tradeEventActionPair, AmendmentSubjectContext primaryAmendmentSubjectContext, List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts) {
        this.amendableFields = amendableFields;
        this.tradeEventActionPair = tradeEventActionPair;
        this.primaryAmendmentSubjectContext = primaryAmendmentSubjectContext;
        this.secondaryAmendmentSubjectContexts = secondaryAmendmentSubjectContexts;
    }

    public Ids computeFirstAmendedCashMessageIdsIfAbsent(FoCashMessage amendmentSubject, Function<FoCashMessage, Ids> computeFunc) {
        if (primaryAmendmentSubjectUpdatedIds == null) {
            primaryAmendmentSubjectUpdatedIds = computeFunc.apply(amendmentSubject);
            return primaryAmendmentSubjectUpdatedIds;
        } else {
            return primaryAmendmentSubjectUpdatedIds;
        }
    }

    public List<AmendableFoCashMessageField> amendableFields() {
        return amendableFields;
    }

    public TradeEventActionPair tradeEventActionPair() {
        return tradeEventActionPair;
    }

    public AmendmentSubjectContext primaryAmendmentSubjectContext() {
        return primaryAmendmentSubjectContext;
    }

    public List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts() {
        return secondaryAmendmentSubjectContexts;
    }
}
