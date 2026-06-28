package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.*;
import io.alw.css.domain.trade.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.domain.ExtendedTrade;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilderResult;
import io.alw.datagen.template.ChildBuildDirective;
import io.alw.datagen.template.ChildBuildDirective.ChildBuildDirectiveType1;
import io.alw.datagen.template.ParentBuildDirective;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static io.alw.css.domain.trade.TradeLegType.CHILD_TRADE;
import static io.alw.css.domain.trade.TradeLegType.PARENT_TRADE;

/// The type parameter M stands for MessageContext which is a combination of [Trade] and its metadata created by the implementations of this class.
/// Some implementations choose to store MessageContext instead of just FoCashMessage in [io.alw.css.fosimulator.store.CashMessageStore]
sealed abstract class CashMessageAmendmentTemplate<T extends ExtendedTrade>
        extends CashMessageTemplate<T>
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

    /// Return Trades(new+amends) which can be consumed by the TradeGenerators and published to CSS
    @Override
    public Set<Trade> get() {
        // Build the trades(newly created + amendments)
        AggregateTemplateBuilderResult<Trade> buildResult =
                ((CashMessageAmendmentTemplate<T>) newBuildCycle())
                        .withMessageAmendments()
                        .withRootTemplateValues()
                        .build();

        // Store the newly created trade(ExtendedTrade) for future amendments if the selection criteria allows to do so
        // The same for amended trades(ExtendedTrade) are already done during the 'related template' build process, via a runnable in the build directive, prior to reaching this point.
        applyFutureAmendmentInclusion(extTrd());

        // Return messages(new+amends) which can be consumed by the TradeGenerators
        var tradeGeneratorInput = new HashSet<>(buildResult.childResults());
        tradeGeneratorInput.add(buildResult.result());
        return Collections.unmodifiableSet(tradeGeneratorInput);
    }

    protected CashMessageAmendmentTemplate<T> withMessageAmendments() {
        // Get cash message data that need to be amended
        final List<T> extTrdsForAmendment = msgStoreHelper().retrieveMessagesForCurrentDay();
        // Add the list of foCashMessages for amendment to the template build step
        for (T extTrd : extTrdsForAmendment) {
            buildCashMessageAmendmentContext(trdAmndCtx -> buildAmendedMessage(trdAmndCtx, extTrd), extTrd);
        }
        return this;
    }

    /// Adds the step to lazily build amended messages in the [io.alw.datagen.template.AggregateTemplateBuilder].
    /// All the cashMessages being amended belongs to the same extTrd
    /// The steps to lazily build(functions) and the actual build done by [io.alw.datagen.template.AggregateTemplateBuilder] are performed in FIFO order
    protected void buildAmendedMessage(TradeAmendmentContext trdAmndCtx, T extTrd) {
        // Amendment if trade is rebooked
        var nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            handleTradeRebookEvent(trdAmndCtx, extTrd);
            return;
        }

        // Trade level amendment if applicable
        final Supplier<TradeBuilder> trdBdrFunc;
        var trdLevelAmndFields = trdAmndCtx.tradeLevelAmendmentFields();
        if (trdLevelAmndFields != null && !trdLevelAmndFields.isEmpty()) {
            trdBdrFunc = () -> buildTradeAmendment(trdAmndCtx, extTrd);
        } else {
            trdBdrFunc = null;
        }

        // TradeLeg level amendment
        List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> trdLegBuildItems = buildTradeLegAmendment(trdAmndCtx, extTrd);
        // Trade and TradeLegs association function
        BiFunction<Trade, Set<TradeLeg>, Trade> tradeAndTradeLegAssociationFunc = Trade::clearAndAddTradeLegs;
        Runnable finalAction = () -> applyFutureAmendmentInclusion(extTrd);
        var buildDirective = new ParentBuildDirective.ParentBuildDirectiveType1<>(trdBdrFunc, trdLegBuildItems, tradeAndTradeLegAssociationFunc, finalAction);
        this.withRelatedTemplateDirective(buildDirective);
    }

    private void handleTradeRebookEvent(TradeAmendmentContext trdAmndCtx, T extTrd) {
        // New trade's Id and Version
        var newTrdId = IdProvider.singleton().nextTradeId();
        var newTrdVer = VERSION_ONE;
        var canTrdVer = extTrd.tradeVersion() + 1;

        // 1. Cancellation for Trade
        final Supplier<TradeBuilder> canTrdBdrFunc = () -> createBuilderFrom(extTrd.trade())
                .tradeVersion(canTrdVer)
                .tradeLinks(List.of(new TradeLink(CHILD_TRADE.name, null, newTrdId, newTrdVer)))
                .tradeEventType(TradeEventType.CANCEL)
                .tradeEventAction(TradeEventAction.ADD);

        // 2. Cancellation for all TradeLegs
        final Supplier<List<TradeLegBuilder>> canTrdLegBdrFunc = () -> {
            var allTrdLegBdrs = new ArrayList<TradeLegBuilder>();
            for (TradeLeg trdLeg : extTrd.allTradeLegs()) {
                var trdLegBdr = TradeLegBuilder.builder(trdLeg)
                        .tradeLegVersion(trdLeg.tradeLegVersion() + 1)
                        .tradeEventType(TradeEventType.CANCEL)
                        .tradeEventAction(TradeEventAction.ADD);
                allTrdLegBdrs.add(trdLegBdr);
            }
            return allTrdLegBdrs;
        };

        // create build directive. There is no need to associate cancelled trade and trade legs to the TradeContext/TradeMetadata/ExtendedTrade. It just needs to be send to downstream system
        BiFunction<Trade, Set<TradeLeg>, Trade> associationFunc = Trade::clearAndAddTradeLegs;
        var canTrdBuildDirective = new ParentBuildDirective.ParentBuildDirectiveType2<>(canTrdBdrFunc, canTrdLegBdrFunc, associationFunc, null);
        this.withRelatedTemplateDirective(canTrdBuildDirective);

        // 3. Create rebooked Trade
        final Supplier<TradeBuilder> newTrdBdrFunc = () -> {
            var newTrdEventAndAction = trdAmndCtx.tradeEventActionPair();
            TradeBuilder newTrdBdr = createBuilderFrom(extTrd.trade());
            return newTrdBdr
                    .tradeID(newTrdId)
                    .tradeVersion(newTrdVer)
                    .tradeLinks(List.of(new TradeLink(PARENT_TRADE.name, null, extTrd.tradeId(), canTrdVer)))
                    .tradeEventType(newTrdEventAndAction.event())
                    .tradeEventAction(newTrdEventAndAction.action())
                    ;
        };
        final Supplier<List<ChildBuildDirective<TradeLeg, TradeLegBuilder>>> newTrdLegBdrFunc = () -> {
            // 4. Reset nextTradeLegId provider so that rebooked trade will have TradeLegs with ids starting from 1
            extTrd.resetTradeLegIdProvider();

            // 5. Create rebooked TradeLegs only for those trade legs present in trdAmndCtx which are explicitly selected by implementation class to be valid for rebooked trade
            return buildTradeLegAmendment(trdAmndCtx, extTrd);
        };

        BiFunction<Trade, Set<TradeLeg>, Trade> newTrdAndTrdLegAssociationFunc = Trade::clearAndAddTradeLegs;
        Consumer<Trade> trdAndExtTrdAssociationFunc = extTrd::setTrade;
        Runnable newTrdBuildDirectiveFinalAction = () -> applyFutureAmendmentInclusion(extTrd);
        var newTrdBuildDirective = new ParentBuildDirective.ParentBuildDirectiveType3<>(newTrdBdrFunc, newTrdLegBdrFunc, newTrdAndTrdLegAssociationFunc, trdAndExtTrdAssociationFunc, newTrdBuildDirectiveFinalAction);
        this.withRelatedTemplateDirective(newTrdBuildDirective);
    }

    private TradeBuilder buildTradeAmendment(TradeAmendmentContext trdAmndCtx, T extTrd) {
        TradeEventActionPair nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        TradeBuilder amndTrdBdr = createBuilderFrom(extTrd.trade());
        // Set new TradeEvent and TradeEventAction
        amndTrdBdr
                .tradeVersion(extTrd.tradeVersion() + 1)
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());

        // Set amended values
        for (AmendableField amendableField : trdAmndCtx.tradeLevelAmendmentFields()) {
            switch (amendableField) {
                case AmendableField.CounterpartyCode _, AmendableField.ValueDate _, AmendableField.Amount _ ->
                        throw new RuntimeException("CounterpartyCode, Amount and ValueDate amendment cannot be done on Trade level. Instead it must be done on the TradeLeg level");
                case AmendableFieldSupplier _ ->
                        throw new RuntimeException("Incorrect use of `AmendableFoCashMessageFieldSupplier` type. `AmendableFoCashMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
            }
        }

        return amndTrdBdr;
    }

    private List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, T extTrd) {
        var buildItems = new ArrayList<ChildBuildDirective<TradeLeg, TradeLegBuilder>>();

        var trdLegAmndCtxs = trdAmndCtx.tradeLegAmendmentContexts();
        for (TradeLegAmendmentContext trdLegAmndCtx : trdLegAmndCtxs) {
            switch (trdLegAmndCtx) {
                case TradeLegAmendmentContextEager trdLegAmndCtxEager -> {
                    var callback = trdLegAmndCtxEager.callback();
                    Supplier<TradeLegBuilder> buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, extTrd);
                    var bi = new ChildBuildDirectiveType1<>(callback, buildStep);
                    buildItems.add(bi);
                }
                case TradeLegAmendmentContextLazy subCtxLazy -> {
                    var bi = buildTradeLegAmendment(trdAmndCtx, subCtxLazy, extTrd);
                    buildItems.addAll(bi);
                }
            }
        }

        return buildItems;
    }

    private List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, TradeLegAmendmentContextLazy trdLegAmndCtxLazy, T extTrd) {
        var buildItems = new ArrayList<ChildBuildDirective<TradeLeg, TradeLegBuilder>>();

        Set<AmendableField> concreteAmendableFields = new HashSet<>();
        for (AmendableField amendableField : trdLegAmndCtxLazy.amendableFields()) {
            switch (amendableField) {
                case AmendableFieldSupplier fieldSupplier -> {
                    switch (fieldSupplier) {
                        case AmendableFieldSupplier.ConditionalSupplier supplier -> {
                            // 1. Evaluate the condition and if applicable proceed with generating the build step
                            var tradeDetail = supplier.conditionSubject();
                            var condition = supplier.condition();
                            if (condition.test(tradeDetail)) {
                                Set<AmendableField> amendableFields = supplier
                                        .amendableFieldSupplierFunctions()
                                        .stream()
                                        .map(func -> func.apply(tradeDetail))
                                        .collect(Collectors.toSet());

                                // 2. Create the amendment subject context with the above derived values
                                TradeLeg tradeLeg = extTrd.getTradeLegFrom(tradeDetail);
                                var trdLegAmndCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, trdLegAmndCtxLazy.callbackProvider().apply(tradeDetail), amendableFields);
                                // 4. Register the build step with the templateBuilder
                                var callback = trdLegAmndCtxEager.callback();
                                Supplier<TradeLegBuilder> buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, extTrd);

                                var bi = new ChildBuildDirectiveType1<>(callback, buildStep);
                                buildItems.add(bi);
                            }
                        }
                        case AmendableFieldSupplier.SupplierWithMessageSelector supplier -> {
                            // 1. Get the amendment subjects
                            List<? extends TradeDetail> trdLegs = supplier.amendmentSubjectSelector().apply(supplier.trdCtx());
                            // 2. Get the fields for amendment for each amendment subject. If there are no amendment subject, nothing is there to build
                            for (TradeDetail tradeDetail : trdLegs) {
                                Set<AmendableField> amendableFields = supplier
                                        .amendableFieldSupplierFunctions()
                                        .stream()
                                        .map(func -> func.apply(tradeDetail))
                                        .collect(Collectors.toSet());

                                // 3. Create the amendment subject context with the above derived values
                                TradeLeg tradeLeg = extTrd.getTradeLegFrom(tradeDetail);
                                var trdLegAmndCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, trdLegAmndCtxLazy.callbackProvider().apply(tradeDetail), amendableFields);
                                // 4. Register the build step with the templateBuilder
                                var callback = trdLegAmndCtxEager.callback();
                                Supplier<TradeLegBuilder> buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, extTrd);

                                var bi = new ChildBuildDirectiveType1<>(callback, buildStep);
                                buildItems.add(bi);
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
            var callbackProvider = trdLegAmndCtxLazy.callbackProvider();

            // Create the amendment subject context
            var trdLegAmndCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, callbackProvider.apply(tradeLeg), concreteAmendableFields);
            // Register the build step with the templateBuilder
            Supplier<TradeLegBuilder> buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, extTrd);

            var bi = new ChildBuildDirectiveType1<>(trdLegAmndCtxEager.callback(), buildStep);
            buildItems.add(bi);
        }

        return buildItems;
    }

    private TradeLegBuilder buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, TradeLegAmendmentContextEager trdLegAmndCtx, T extTrd) {
        TradeEventActionPair nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        Set<AmendableField> amendableFields = trdLegAmndCtx.amendableFields();
        TradeLeg tradeLeg = trdLegAmndCtx.tradeLeg();
        // Create builder for amending cashMessage from the cashMessage being amended
        TradeLegBuilder amndBdr = TradeLegBuilder.builder(tradeLeg);

        // If trade is rebooked, get a new tradeLegId. Note: the extTrd::nextTradeLegId counter was already reset in a previous step due to rebook event
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            amndBdr
                    .tradeLegId(extTrd.nextTradeLegId())
                    .tradeLegVersion(VERSION_ONE);
        }
        // If not rebooked, just increment the tradeLegVersion
        else {
            amndBdr
                    .tradeLegVersion(tradeLeg.tradeLegVersion() + 1);
        }
        // set new trade even and action
        amndBdr
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());

        // Set amended values
        for (AmendableField amendableField : amendableFields) {
            switch (amendableField) {
                case AmendableField.ValueDate(var newValueDate) -> amndBdr.valueDate(newValueDate);
                case AmendableField.Amount(var newAmount) -> amndBdr.amount(newAmount);
                case AmendableField.CounterpartyCode(var cpCode) -> amndBdr.counterpartyCode(cpCode);
                case AmendableFieldSupplier _ -> {
                    throw new RuntimeException("Incorrect use of `AmendableFoCashMessageFieldSupplier` type. `AmendableFoCashMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
                }
            }
        }

        return amndBdr;
    }

    private TradeBuilder createBuilderFrom(Trade trd) {
        return TradeBuilder.builder(trd);
    }

    private void applyFutureAmendmentInclusion(T extTrade) {
        if (amendmentCandidateSelectionCriteria().test(extTrade)) {
            msgStoreHelper().storeMessageDataForFutureRndmRetrievalDay(extTrade);
        }
    }
}
