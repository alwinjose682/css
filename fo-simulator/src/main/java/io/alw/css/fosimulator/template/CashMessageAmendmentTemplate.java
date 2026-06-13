package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.*;
import io.alw.css.domain.trade.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.domain.TradeMetadata;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilderResult;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static io.alw.css.domain.trade.TradeLegType.CHILD_CASHFLOW;
import static io.alw.css.domain.trade.TradeLegType.PARENT_CASHFLOW;

/// The type parameter M stands for MessageContext which is a combination of [Trade] and its metadata created by the implementations of this class.
/// Some implementations choose to store MessageContext instead of just FoCashMessage in [io.alw.css.fosimulator.store.CashMessageStore]
sealed abstract class CashMessageAmendmentTemplate<T extends TradeMetadata>
        extends CashMessageTemplate<T, T>
        permits FxTemplate, MmTemplate {

    public CashMessageAmendmentTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    /// Both primary and secondary criteria will be applied to select a tradeContext for amendment.
    /// This applies both for the selecting a tradeContext for first time and each time after amendment
    private final Predicate<T> amendmentCandidateSelectionCriteriaPrimary = trd -> {
        var rootTradeLeg = trd.rootTradeLeg();
        return trd.tradeEventType() != TradeEventType.CANCEL
                && trd.tradeEventType() != TradeEventType.REBOOK
                && (rootTradeLeg.tradeLegVersion() + trd.tradeVersion() <= msgTemplateHelper.cashMsgTemplateProps.maxPermittedAmendments());
    };

    /// see also{@link CashMessageAmendmentTemplate#amendmentCandidateSelectionCriteriaPrimary}
    private Predicate<T> amendmentCandidateSelectionCriteria() {
        return amendmentCandidateSelectionCriteriaPrimary.and(tradeContextAmendmentFrequency());
    }

    /// The tradeContext amendment frequency is the secondary amendmentCandidateSelectionCriteria.
    /// see also {@link CashMessageAmendmentTemplate#amendmentCandidateSelectionCriteriaPrimary}
    protected abstract Predicate<T> tradeContextAmendmentFrequency();

    /// All the cashMessages selected for amendment must belong only to the trdCtx being passed via this method
    protected abstract void buildCashMessageAmendmentContext(Consumer<TradeAmendmentContext> buildAmendedMessageFunc, T trdCtxForAmendment);

    protected abstract CashMessageStoreHelper<T> msgStoreHelper();

    /// Return cash messages(new+amends) which can be consumed by the CashflowGenerators and published to CSS
    @Override
    public List<Trade> get() {
        // Build the cash messages(newly created + amendments)
        AggregateTemplateBuilderResult<T, TradeLeg> buildResult =
                ((CashMessageAmendmentTemplate<T>) newBuildCycle())
                        .withMessageAmendments()
                        .withRootTemplateValues()
                        .build();

        T trdCtx = buildResult.root();
        // Store the newly created tradeContext for future amendments if the selection criteria allows to do so
        applyFutureAmendmentInclusion(trdCtx);

        // Return messages(new+amends) which can be consumed by the CashflowGenerators
        var cashflowGnrtrInput = new ArrayList<TradeLeg>();
        cashflowGnrtrInput.add(trdCtx.rootTradeLeg());
        cashflowGnrtrInput.addAll(buildResult.grouped());
        cashflowGnrtrInput.addAll(buildResult.related());

        return Collections.unmodifiableList(cashflowGnrtrInput);
    }

    protected CashMessageAmendmentTemplate<T> withMessageAmendments() {
        // Get cash message data that need to be amended
        final List<T> trdCtxsForAmendment = msgStoreHelper().retrieveMessagesForCurrentDay();
        // Add the list of foCashMessages for amendment to the template build step
        for (T trdCtx : trdCtxsForAmendment) {
            buildCashMessageAmendmentContext(trdAmndCtx -> buildAmendedMessage(trdAmndCtx, trdCtx), trdCtx);
        }
        return this;
    }

    /// Adds the step to lazily build amended messages in the [io.alw.datagen.template.AggregateTemplateBuilder].
    /// All the cashMessages being amended belongs to the same trd
    /// The steps to lazily build(functions) and the actual build done by [io.alw.datagen.template.AggregateTemplateBuilder] are performed in FIFO order
    protected void buildAmendedMessage(TradeAmendmentContext trdAmndCtx, T trd) {
        var nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            handleTradeRebookEvent(trdAmndCtx, trd);
            return;
        }

        // TODO: Which one should be built first? Trade or TradeLeg

        var trdLegAmndCtxs = trdAmndCtx.tradeLegAmendmentContexts();
        for (TradeLegAmendmentContext trdLegAmndCtx : trdLegAmndCtxs) {
            switch (trdLegAmndCtx) {
                case TradeLegAmendmentContextEager subCtxEager -> this.withRelatedItem(
                        subCtxEager.callback(),
                        () -> applyFutureAmendmentInclusion(trd),
                        () -> buildTradeLegAmendment(trdAmndCtx, subCtxEager, trd));
                case TradeLegAmendmentContextLazy subCtxLazy -> this.withRelatedItem(() -> buildTradeLegAmendment(trdAmndCtx, subCtxLazy, trd));
            }
        }
    }

    private void handleTradeRebookEvent(TradeAmendmentContext trdAmndCtx, T trd) {
        // TODO: Which one should be built first? Trade or TradeLeg

        // 1. Create cancellation for Trade
        TradeBuilder canTrdBdr = createBuilderFrom(trd.trade());

        // 2. Create cancellation for all TradeLegs
        //TODO: What to do for TradeLegs like InterestTradeLeg that extends TradeLeg?
        for (TradeLeg trdLeg : trd.allTradeLegs()) {
            TradeLegBuilder trdLegBdr = TradeLegBuilder.builder(trdLeg);
        }

        // 3. Create rebooked Trade
        TradeBuilder newTrdBdr = createBuilderFrom(trd.trade());

        // 4. Create rebooked TradeLegs only for those trade legs present in trdAmndCtx which are explicitly selected by implementation class to be valid for rebooked trade
        for (TradeLegAmendmentContext tradeLegAmendmentContext : trdAmndCtx.tradeLegAmendmentContexts()) {
            // TODO: Increment tradeLegAmendment or should it be supplied to this method as an Id object because this method may be invoked due to a rebook event ?
        }
    }

    private void buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, TradeLegAmendmentContextLazy trdLegAmndCtxLazy, T trd) {
        Set<AmendableField> concreteAmendableFields = new HashSet<>();

        for (AmendableField amendableField : trdLegAmndCtxLazy.amendableFields()) {
            switch (amendableField) {
                case AmendableFieldSupplier fieldSupplier -> {
                    switch (fieldSupplier) {
                        case AmendableFieldSupplier.ConditionalSupplier supplier -> {
                            // 1. Evaluate the condition and if applicable proceed with generating the build step
                            var tradeLeg = supplier.conditionSubject();
                            var condition = supplier.condition();
                            if (condition.test(tradeLeg)) {
                                Set<AmendableField> amendableFields = supplier
                                        .amendableFieldSupplierFunctions()
                                        .stream()
                                        .map(func -> func.apply(tradeLeg))
                                        .collect(Collectors.toSet());

                                // 2. Create the amendment subject context with the above derived values
                                var trdLegAmndCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, trdLegAmndCtxLazy.callbackProvider().apply(tradeLeg), amendableFields);
                                // 4. Register the build step with the templateBuilder
                                this.withRelatedItem(
                                        trdLegAmndCtxEager.callback(),
                                        () -> applyFutureAmendmentInclusion(trd),
                                        () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, trd));
                            }
                        }
                        case AmendableFieldSupplier.SupplierWithMessageSelector supplier -> {
                            // 1. Get the amendment subjects
                            List<? extends TradeLeg> cashLegs = supplier.amendmentSubjectSelector().apply(supplier.trdCtx());
                            // 2. Get the fields for amendment for each amendment subject. If there are no amendment subject, nothing is there to build
                            for (TradeLeg tradeLeg : cashLegs) {
                                Set<AmendableField> amendableFields = supplier
                                        .amendableFieldSupplierFunctions()
                                        .stream()
                                        .map(func -> func.apply(tradeLeg))
                                        .collect(Collectors.toSet());

                                // 3. Create the amendment subject context with the above derived values
                                var trdLegAmndCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, trdLegAmndCtxLazy.callbackProvider().apply(tradeLeg), amendableFields);
                                // 4. Register the build step with the templateBuilder
                                this.withRelatedItem(
                                        trdLegAmndCtxEager.callback(),
                                        () -> applyFutureAmendmentInclusion(trd),
                                        () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, trd));
                            }
                        }
                    }
                }
                case AmendableField.Amount _, AmendableField.CounterpartyCode _, AmendableField.ValueDate _ -> {
                    concreteAmendableFields.add(amendableField);
                }
            }
        }

        // NOTE: AmendmentSubjectContextLazy may contain a both concrete amendable fields like AmendableFoCashMessageField.Amount, ValueDate etc in addition to the obviously expected 'AmendableFoCashMessageFieldSupplier'
        if (!concreteAmendableFields.isEmpty()) {
            var tradeLeg = trdLegAmndCtxLazy.tradeLeg();
            if (tradeLeg == null) {
                throw new RuntimeException("Unable to amend a cashMessage. tradeLeg should not be null when a concrete 'AmendableFoCashMessageField' is present in 'AmendmentSubjectContextLazy'");
            }
            var callback = trdLegAmndCtxLazy.callbackProvider().apply(tradeLeg);

            // Create the amendment subject context
            var amndSubCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, callback, concreteAmendableFields);
            // Register the build step with the templateBuilder
            this.withRelatedItem(
                    amndSubCtxEager.callback(),
                    () -> applyFutureAmendmentInclusion(trd),
                    () -> buildTradeLegAmendment(trdAmndCtx, amndSubCtxEager, trd));
        }
    }

    private TradeLegBuilder buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, TradeLegAmendmentContextEager trdLegAmndCtx, T trd) {
        Set<AmendableField> amendableFields = trdLegAmndCtx.amendableFields();

        TradeEventActionPair nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        TradeLeg tradeLeg = trdLegAmndCtx.tradeLeg();
        // Create builder for amending cashMessage from the cashMessage being amended
        TradeLegBuilder amndBdr = createBuilderFrom(tradeLeg);

        // If trade is rebooked, create a cancellation of the tradeLeg in addition to the steps specific for rebook event
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            handleTradeRebookEventForTradeLeg(trdAmndCtx, amndBdr, trd);
        }

        // TODO: Increment tradeLegAmendment or should be supplied to this method as an Id object because this method may be invoked due to a rebook event ?

        // Set new TradeEvent and TradeEventAction
        amndBdr
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());

        // Set amended values
        for (AmendableField amendableField : amendableFields) {
            switch (amendableField) {
                case AmendableField.ValueDate(var newValueDate) -> amndBdr.valueDate(newValueDate);
                case AmendableField.Amount(var newAmount) -> amndBdr.amount(newAmount);
                case AmendableField.CounterpartyCode _ -> throw new RuntimeException("CounterpartyCode amendment cannot be done on TradeLeg level. Instead it must be done on the Trade level");
                case AmendableFieldSupplier _ -> {
                    throw new RuntimeException("Incorrect use of `AmendableFoCashMessageFieldSupplier` type. `AmendableFoCashMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
                }
            }
        }

        return amndBdr;
    }

    private TradeBuilder buildTradeAmendment(TradeAmendmentContext trdAmndCtx, T trd) {
        TradeEventActionPair nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        // If NOT rebooked, increments the cashflow version and randomly chooses to increment the trade version.
        // These same values used for the first message are used for the subsequent messages being amended that belong to the same MessageContext
        // The TradeEventType of the root and the related cashMessages being amended will be same. If not same, then Ids assigned to the amended cashflow will go wrong
        // (Ex: amending PRINCIPAL of an MM trade for TradeEventType#REBOOK, may result in amending MATURITY leg with the same TradeEventType as REBOOK)

        TradeBuilder amndTrdBdr = createBuilderFrom(trd.trade());

        // If trade is rebooked, create a cancellation of the trade(not for tradeLeg) in addition to the steps specific for rebook event
        // Do not do the same for tradeLeg here. For a rebook event, the tradeLegs that need to be
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            handleTradeRebookEvent(trdAmndCtx, amndTrdBdr, trd);
        }

        // Set new TradeEvent and TradeEventAction
        amndTrdBdr
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());

        // Set amended values
        for (AmendableField amendableField : trdAmndCtx.tradeLevelAmendmentFields()) {
            switch (amendableField) {
                case AmendableField.CounterpartyCode(var newCounterpartyCode) -> {
                    amndTrdBdr
                            .tradeVersion(trd.tradeVersion() + 1)
                            .counterpartyCode(newCounterpartyCode);
                }
                case AmendableField.ValueDate _, AmendableField.Amount _ ->
                        throw new RuntimeException("Amount and ValueDate amendment cannot be done on Trade level. Instead it must be done on the TradeLeg level");
                case AmendableFieldSupplier _ ->
                        throw new RuntimeException("Incorrect use of `AmendableFoCashMessageFieldSupplier` type. `AmendableFoCashMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
            }
        }


        return amndTrdBdr;
    }

    /// 1) creates a new cashMessage for a new trade
    /// 2) creates cashflow to cancel the original cashMessage and adds to the builder without callback. The callback needs to be used for the new cashMessage
    /// 3) the new cashMessage corresponding to the new trade is associated with the same [TradeMetadata] -
    /// Nothing has to be done explicitly to ensure this. The callback although created for the old cashMessage will be applied to the new cashMessage.
    private void handleTradeRebookEvent123(TradeAmendmentContext amndCtx, TradeBuilder amndBdr, T trdCtx) {
        TradeLeg amendmentSubject = amndSubjectCtx.amendmentSubject();
        TradeLegType amendmentSubjectLinkType = amndSubjectCtx.tradeLegType();

        // 1. Create new trade ID and cashflow ID
        // These same values used for the first message are used for the subsequent messages being amended that belong to the same MessageContext
        var rebookedTradeIds = amndCtx.computeFirstAmendedCashMessageIdsIfAbsent(amendmentSubject, (_) -> {
            IdProvider idProvider = IdProvider.singleton();
            final long rebookedTradeID = idProvider.nextTradeId();
            return new Id(rebookedTradeID, VERSION_ONE);
        });

        final int cancelledCashflowVersion = amendmentSubject.cashflowVersion() + 1;
        final int cancelledTradeVersion = amendmentSubject.tradeVersion();

        // 2. Create cancellation for the original cashflow and register in the TemplateBuilder
        this.withRelatedItem(() -> {
            // Add trade link that corresponds to rebooked cashflow to the existing list of trade links
            List<TradeLink> newTradeLinks = amendmentSubject.tradeLinks() != null && !amendmentSubject.tradeLinks().isEmpty() ? new ArrayList<>(amendmentSubject.tradeLinks()) : new ArrayList<>();
            newTradeLinks.add(TradeLinkBuilder.TradeLink(
                    CHILD_CASHFLOW.name, null,
                    rebookedTradeIds.Id(), rebookedTradeIds.version(),
                    rebookedTradeIds.tradeID(), rebookedTradeIds.tradeVersion()));

            // Create builder for cashflow cancellation
            return createBuilderFrom(amendmentSubject)
                    // Id Version
                    .tradeVersion(cancelledTradeVersion)
                    .cashflowVersion(cancelledCashflowVersion)
                    // Trade Event and Action
                    .tradeEventType(TradeEventType.CANCEL)
                    .tradeEventAction(TradeEventAction.ADD)
                    .tradeLinks(Collections.unmodifiableList(newTradeLinks));
        });

        // 3. Create the new trade and cashflow (because of trade rebook)
        List<TradeLink> newTradeLinks = new ArrayList<>();
        // Add trade link of cancelled cashflow
        newTradeLinks.add(TradeLinkBuilder.TradeLink(
                PARENT_CASHFLOW.name, null,
                amendmentSubject.cashflowID(), cancelledCashflowVersion,
                amendmentSubject.tradeID(), cancelledTradeVersion));
        // Add trade link of this new cashflow
        newTradeLinks.add(TradeLinkBuilder.TradeLink(
                amendmentSubjectLinkType.name, null,
                rebookedTradeIds.Id(), rebookedTradeIds.version(),
                rebookedTradeIds.tradeID(), rebookedTradeIds.tradeVersion()));

        // Note: This amndBdr is used further in the caller method to set other fields like the amended field, trade event and action etc
        amndBdr
                // Id Version
                .tradeID(rebookedTradeIds.tradeID())
                .tradeVersion(rebookedTradeIds.tradeVersion())
                .cashflowID(rebookedTradeIds.Id())
                .cashflowVersion(rebookedTradeIds.version())
                .tradeLinks(Collections.unmodifiableList(newTradeLinks));

    }

    private TradeBuilder createBuilderFrom(Trade trd) {
        return TradeBuilder.builder(trd);
    }

    private void applyFutureAmendmentInclusion(T trdCtx) {
        if (amendmentCandidateSelectionCriteria().test(trdCtx)) {
            msgStoreHelper().storeMessageDataForFutureRndmRetrievalDay(trdCtx);
        }
    }
}
