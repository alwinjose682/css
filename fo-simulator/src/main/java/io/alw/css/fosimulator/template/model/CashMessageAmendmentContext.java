package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.TradeEventActionPair;

import java.util.List;
import java.util.function.Function;

/// Note: The `primaryAmendmentSubject` is the cashMessage selected for amendment that has caused amendment for other dependent cashMessages
public final class CashMessageAmendmentContext {
    private final TradeEventActionPair tradeEventActionPair;
    private final AmendmentSubjectContext primaryAmendmentSubjectContext;
    private final List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts;
    private Ids primaryAmendmentSubjectUpdatedIds;

    public CashMessageAmendmentContext(TradeEventActionPair tradeEventActionPair, AmendmentSubjectContext primaryAmendmentSubjectContext, List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts) {
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
