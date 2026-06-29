package io.alw.css.tradepublisher.template;

import io.alw.css.domain.common.*;
import io.alw.css.domain.trade.*;
import io.alw.css.tradepublisher.model.Entity;
import io.alw.css.tradepublisher.model.TradeEventActionPair;
import io.alw.css.tradepublisher.model.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.service.RefDataService;
import io.alw.css.tradepublisher.template.domain.ExtendedTrade;
import io.alw.css.tradepublisher.template.model.*;
import io.alw.css.tradepublisher.tradegenerator.DayTicker;
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
sealed abstract class TradeAmendmentTemplate<T extends ExtendedTrade>
        extends TradeTemplate<T>
        permits FxTemplate, MmTemplate {

    public TradeAmendmentTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, TradeTemplateProperties trdTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, trdTemplateProps);
    }

    /// Both primary and secondary criteria will be applied to select a tradeContext for amendment.
    /// This applies both for the selecting a tradeContext for first time and each time after amendment
    private final Predicate<T> amendmentCandidateSelectionCriteriaPrimary = trd -> {
        var rootTradeLeg = trd.rootTradeLeg();
        return trd.tradeEventType() != TradeEventType.CANCEL
                && trd.tradeEventType() != TradeEventType.REBOOK
                && (rootTradeLeg.tradeLegVersion() + trd.tradeVersion() <= msgTemplateHelper.trdTemplateProps.maxPermittedAmendments());
    };

    /// see also{@link TradeAmendmentTemplate#amendmentCandidateSelectionCriteriaPrimary}
    private Predicate<T> amendmentCandidateSelectionCriteria() {
        return amendmentCandidateSelectionCriteriaPrimary.and(tradeContextAmendmentFrequency());
    }

    protected abstract List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> withNewTradeLegDirectives(T extTrd);

    /// The tradeContext amendment frequency is the secondary amendmentCandidateSelectionCriteria.
    /// see also {@link TradeAmendmentTemplate#amendmentCandidateSelectionCriteriaPrimary}
    protected abstract Predicate<T> tradeContextAmendmentFrequency();

    /// All the trade legs selected for amendment must belong only to the trdCtx being passed via this method
    protected abstract void buildTradeAmendmentContext(Consumer<TradeAmendmentContext> buildAmendedMessageFunc, T trdCtxForAmendment);

    protected abstract TradeStoreHelper<T> trdStoreHelper();

    /// Return Trades(new+amends) which can be consumed by the TradeGenerators and published to CSS
    @Override
    public Set<Trade> get() {
        // Build the trades(newly created + amendments)
        AggregateTemplateBuilderResult<Trade> buildResult =
                ((TradeAmendmentTemplate<T>) newBuildCycle())
                        .withMessageAmendmentsAndNewTradeLegs()
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

    private TradeAmendmentTemplate<T> withMessageAmendmentsAndNewTradeLegs() {
        var extTrdAndBuildDirMap = createTradeAmendmentDirectives();
        createNewTradeLegDirectivesAndAddTo(extTrdAndBuildDirMap);

        // Register the amendmentTradeBuildDirective with the template builder
        extTrdAndBuildDirMap.values().forEach(this::withRelatedTemplateDirective);
        return this;
    }

    private void createNewTradeLegDirectivesAndAddTo(Map<T, ParentBuildDirective<Trade, TradeLeg, TradeBuilder, TradeLegBuilder>> extTrdAndBuildDirMap) {
        // 1. Get trades for which new TradeLegs need to be created
        final List<T> extTrds = trdStoreHelper().retrieveTradesForCurrentDay(TradeStoreHelper.TradeRetrievalPurpose.NEW_TRD_LEG);
        for (T extTrd : extTrds) {
            // 2. Create build directive for new TradeLegs creation
            final List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> newTradeLegDirectives = withNewTradeLegDirectives(extTrd);
            // 3. Add the new TradeLeg directives to the amendmentTradeBuildDirective so that a single Trade object will be created with amended TradeLegs and newly created TradeLegs
            final var buildDirective = extTrdAndBuildDirMap.get(extTrd);
            if (buildDirective != null) {
                buildDirective.adhocChildDirectives().addAll(newTradeLegDirectives);
            } else {
                // Trade Builder function. No change to the Trade
                final Supplier<TradeBuilder> trdBdrFunc = () -> createBuilderFrom(extTrd.trade());
                // Trade and TradeLegs association function
                BiFunction<Trade, Set<TradeLeg>, Trade> tradeAndTradeLegAssociationFunc = Trade::clearAndAddTradeLegs;
                var newAdhocBuildDirective = new ParentBuildDirective.ParentBuildDirectiveType1<>(trdBdrFunc, newTradeLegDirectives, tradeAndTradeLegAssociationFunc, null);
                extTrdAndBuildDirMap.put(extTrd, newAdhocBuildDirective);
            }
        }
    }

    private Map<T, ParentBuildDirective<Trade, TradeLeg, TradeBuilder, TradeLegBuilder>> createTradeAmendmentDirectives() {
        // 1. Get trades that need to be amended
        final List<T> extTrds = trdStoreHelper().retrieveTradesForCurrentDay(TradeStoreHelper.TradeRetrievalPurpose.AMEND);
        // 2. Create trade amendment directive

        Map<T, ParentBuildDirective<Trade, TradeLeg, TradeBuilder, TradeLegBuilder>> extTrdAndBuildDirMap = new HashMap<>();
        for (T extTrd : extTrds) {
            buildTradeAmendmentContext(trdAmndCtx -> {
                // 1. Create build directive for message amendments
                var tradeAmendmentBuildDirective = createTradeAmendmentDirective(trdAmndCtx, extTrd);
                extTrdAndBuildDirMap.put(extTrd, tradeAmendmentBuildDirective);
            }, extTrd);
        }

        return extTrdAndBuildDirMap;
    }

    /// Adds the step to lazily build amended messages in the [io.alw.datagen.template.AggregateTemplateBuilder].
    /// All the trade legs being amended belong to the same extTrd
    /// The steps to lazily build(functions) and the actual build done by [io.alw.datagen.template.AggregateTemplateBuilder] are performed in FIFO order
    protected ParentBuildDirective<Trade, TradeLeg, TradeBuilder, TradeLegBuilder> createTradeAmendmentDirective(TradeAmendmentContext trdAmndCtx, T extTrd) {
        // Amendment if trade is rebooked
        var nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            return handleTradeRebookEvent(trdAmndCtx, extTrd);
        }

        // Trade level amendment if applicable
        final Supplier<TradeBuilder> trdBdrFunc;
        var trdLevelAmndFields = trdAmndCtx.tradeLevelAmendmentFields();
        if (trdLevelAmndFields != null && !trdLevelAmndFields.isEmpty()) {
            trdBdrFunc = () -> buildTradeAmendment(trdAmndCtx, extTrd);
        } else {
            trdBdrFunc = () -> createBuilderFrom(extTrd.trade()); // No change to any fields of the trade, not even the trade version.
        }

        // TradeLeg level amendment
        List<ChildBuildDirective<TradeLeg, TradeLegBuilder>> trdLegBuildItems = buildTradeLegAmendment(trdAmndCtx, extTrd);
        // Trade and TradeLegs association function
        BiFunction<Trade, Set<TradeLeg>, Trade> tradeAndTradeLegAssociationFunc = Trade::clearAndAddTradeLegs;
        Runnable finalAction = () -> applyFutureAmendmentInclusion(extTrd);
        return new ParentBuildDirective.ParentBuildDirectiveType1<>(trdBdrFunc, trdLegBuildItems, tradeAndTradeLegAssociationFunc, finalAction);
    }

    private ParentBuildDirective<Trade, TradeLeg, TradeBuilder, TradeLegBuilder> handleTradeRebookEvent(TradeAmendmentContext trdAmndCtx, T extTrd) {
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
                if (trdLeg == null) { // could be null, ex: Maturity of an MM call trade
                    continue;
                }
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
        return new ParentBuildDirective.ParentBuildDirectiveType3<>(newTrdBdrFunc, newTrdLegBdrFunc, newTrdAndTrdLegAssociationFunc, trdAndExtTrdAssociationFunc, newTrdBuildDirectiveFinalAction);
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
                        throw new RuntimeException("Incorrect use of `AmendableTradeMessageFieldSupplier` type. `AmendableTradeMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
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

        // NOTE: AmendmentSubjectContextLazy may contain a both concrete amendable fields like AmendableTradeMessageField.Amount, ValueDate etc in addition to the obviously expected 'AmendableTradeMessageFieldSupplier'
        if (!concreteAmendableFields.isEmpty()) {
            var tradeLeg = trdLegAmndCtxLazy.tradeLeg();
            if (tradeLeg == null) {
                throw new RuntimeException("Unable to amend a trade leg. tradeLeg should not be null when a concrete 'AmendableTradeMessageField' is present in 'AmendmentSubjectContextLazy'");
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
        // Create builder for amending trade leg
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
                    throw new RuntimeException("Incorrect use of `AmendableTradeMessageFieldSupplier` type. `AmendableTradeMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
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
            trdStoreHelper().storeTradeForFutureRndmRetrievalDay(extTrade, TradeStoreHelper.TradeRetrievalPurpose.AMEND);
        }
    }
}
