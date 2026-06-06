package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.TradeEventActionPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/// All amendmentSubjectContexts are held in an ArrayList to ensure strict sequential encounter order(FIFO) when retrieving them.
/// This is important because, amendmentSubjects are built lazily and may require the previously built amendmentSubjects during their build process
///
/// Note: The primaryAmendmentSubject is ideally the cashMessage selected for amendment that has caused amendment for other dependent cashMessages.
/// But it is not explicitly ensured by this class as to which element is the primaryAmendmentSubject.
/// It is the responsibility of the user to add the elements in the proper simple sequential order using [CashMessageAmendmentContext#addNextAmndSubCtx]
public final class CashMessageAmendmentContext {
    private final TradeEventActionPair tradeEventActionPair;
    private final List<AmendmentSubjectContext> amendmentSubjectContexts;
    private Ids firstAmendedSubjectUpdatedIds;

    public CashMessageAmendmentContext(TradeEventActionPair tradeEventActionPair) {
        this.tradeEventActionPair = tradeEventActionPair;
        this.amendmentSubjectContexts = new ArrayList<>();
    }

    public Ids computeFirstAmendedCashMessageIdsIfAbsent(FoCashMessage amendmentSubject, Function<FoCashMessage, Ids> computeFunc) {
        if (firstAmendedSubjectUpdatedIds == null) {
            firstAmendedSubjectUpdatedIds = computeFunc.apply(amendmentSubject);
            return firstAmendedSubjectUpdatedIds;
        } else {
            return firstAmendedSubjectUpdatedIds;
        }
    }

    /// The order in which elements are added determines the encounter order as well(FIFO).
    /// see {@link CashMessageAmendmentContext}
    public CashMessageAmendmentContext addNextAmndSubCtx(AmendmentSubjectContext amndSubCtx) {
        amendmentSubjectContexts.add(amndSubCtx);
        return this;
    }

    public TradeEventActionPair tradeEventActionPair() {
        return tradeEventActionPair;
    }

    public List<AmendmentSubjectContext> amendmentSubjectContexts() {
        return Collections.unmodifiableList(amendmentSubjectContexts);
    }
}
