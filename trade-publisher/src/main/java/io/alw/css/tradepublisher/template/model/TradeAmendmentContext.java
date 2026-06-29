package io.alw.css.tradepublisher.template.model;

import io.alw.css.domain.trade.Trade;
import io.alw.css.tradepublisher.model.TradeEventActionPair;
import io.alw.css.tradepublisher.template.domain.ExtendedTrade;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/// All amendmentSubjectContexts are held in an ArrayList to ensure strict sequential encounter order(FIFO) when retrieving them.
/// This is important because, amendmentSubjects are built lazily and may require the previously built amendmentSubjects during their build process
///
/// Note: The primaryAmendmentSubject is the trade or trade leg selected for amendment that has caused amendment for others.
/// It is the responsibility of the user to add the elements in the proper simple sequential order using [TradeAmendmentContext#addNextTradeLegAmndCtx]
public final class TradeAmendmentContext {
    private final Set<AmendableField> tradeLevelAmendmentFields;
    private final Consumer<Trade> callback;
    private final TradeEventActionPair tradeEventActionPair;
    private final List<TradeLegAmendmentContext> tradeLegAmendmentContexts;
    private Id firstAmendedSubjectUpdatedId;

    public TradeAmendmentContext(TradeEventActionPair tradeEventActionPair) {
        this(tradeEventActionPair, new HashSet<>(), _ -> {
        });
    }

    public TradeAmendmentContext(TradeEventActionPair tradeEventActionPair, Set<AmendableField> tradeLevelAmendmentFields, Consumer<Trade> callback) {
        if (tradeEventActionPair == null || tradeLevelAmendmentFields == null || callback == null) {
            throw new RuntimeException("tradeEventActionPair and tradeLevelAmendmentFields cannot be null");
        }
        this.tradeEventActionPair = tradeEventActionPair;
        this.tradeLevelAmendmentFields = tradeLevelAmendmentFields;
        this.callback = callback;
        this.tradeLegAmendmentContexts = new ArrayList<>();
    }

    public Id computeFirstAmendedCashMessageIdsIfAbsent(ExtendedTrade amendmentSubject, Function<ExtendedTrade, Id> computeFunc) {
        if (firstAmendedSubjectUpdatedId == null) {
            firstAmendedSubjectUpdatedId = computeFunc.apply(amendmentSubject);
            return firstAmendedSubjectUpdatedId;
        } else {
            return firstAmendedSubjectUpdatedId;
        }
    }

    /// The order in which elements are added determines the encounter order as well(FIFO).
    /// If the [TradeLegAmendmentContext] is null, then it is not added to the Set
    /// see {@link TradeAmendmentContext}
    public TradeAmendmentContext addNextTradeLegAmndCtx(TradeLegAmendmentContext amndSubCtx) {
        if (amndSubCtx != null) {
            tradeLegAmendmentContexts.add(amndSubCtx);
        }
        return this;
    }

    public TradeEventActionPair tradeEventActionPair() {
        return tradeEventActionPair;
    }

    public List<TradeLegAmendmentContext> tradeLegAmendmentContexts() {
        return Collections.unmodifiableList(tradeLegAmendmentContexts);
    }

    public Set<AmendableField> tradeLevelAmendmentFields() {
        return tradeLevelAmendmentFields;
    }
}
