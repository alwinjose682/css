package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.*;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeBuilder;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegBuilder;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.domain.TradeMetadata;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilderResult;
import io.alw.datagen.template.BuildItem;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static io.alw.css.domain.trade.TradeLegType.CHILD_TRADE;
import static io.alw.css.domain.trade.TradeLegType.PARENT_TRADE;

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
        // Amendment if trade is rebooked
        var nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            handleTradeRebookEvent(trdAmndCtx, trd);
            return;
        }

        // Trade level amendment if applicable
        final Supplier<TradeBuilder> trdBdrFunc;
        final Supplier<? extends TradeMetadata> trdSupplierFunc;
        var trdLevelAmndFields = trdAmndCtx.tradeLevelAmendmentFields();
        if (trdLevelAmndFields != null && !trdLevelAmndFields.isEmpty()) {
            trdBdrFunc = () -> buildTradeAmendment(trdAmndCtx, trd);
            trdSupplierFunc = null;
        } else {
            trdBdrFunc = null;
            trdSupplierFunc = () -> trd;
        }

        // TradeLeg level amendment
        var trdLegBuildItems = buildTradeLegAmendment(trdAmndCtx, trd);
    }

    private List<BuildItem<>> buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, T trd) {
        List<BuildItem<>> buildItems = new ArrayList<>();

        var trdLegAmndCtxs = trdAmndCtx.tradeLegAmendmentContexts();
        for (TradeLegAmendmentContext trdLegAmndCtx : trdLegAmndCtxs) {
            switch (trdLegAmndCtx) {
                case TradeLegAmendmentContextEager trdLegAmndCtxEager -> {
                    var callback = trdLegAmndCtxEager.callback();
                    var runnableAfterCallback = () -> applyFutureAmendmentInclusion(trd);
                    var buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, trd);
                    var bi = new BuildItem<>(callback, runnableAfterCallback, buildStep);
                    buildItems.add(bi);
                }
                case TradeLegAmendmentContextLazy subCtxLazy -> {
                    var bi = buildTradeLegAmendment(trdAmndCtx, subCtxLazy, trd);
                    buildItems.add(bi);
                }
            }
        }
    }

    private TradeBuilder buildTradeAmendment(TradeAmendmentContext trdAmndCtx, T trd) {
        TradeEventActionPair nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        TradeBuilder amndTrdBdr = createBuilderFrom(trd.trade());
        // Set new TradeEvent and TradeEventAction
        amndTrdBdr
                .tradeVersion(trd.tradeVersion() + 1)
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

    private void handleTradeRebookEvent(TradeAmendmentContext trdAmndCtx, T trd) {
        // New trade's Id and Version
        var newTrdId = IdProvider.singleton().nextTradeId();
        var newTrdVer = VERSION_ONE;
        var canTrdVer = trd.tradeVersion() + 1;

        // 1. Create cancellation for Trade and register in the TemplateBuilder
        this.withRelatedItem(
                trd::consumeAmendedTradeLegs,
                trd::setTrade,
                () -> {
                    TradeBuilder canTrdBdr = createBuilderFrom(trd.trade());
                    return canTrdBdr
                            .tradeVersion(canTrdVer)
                            .tradeLinks(List.of(new TradeLink(CHILD_TRADE.name, null, 0, 0, newTrdId, newTrdVer)))
                            .tradeEventType(TradeEventType.CANCEL)
                            .tradeEventAction(TradeEventAction.ADD)
                            ;
                },
                // 2. Create cancellation for all TradeLegs
                () -> {
                    for (TradeLeg trdLeg : trd.allTradeLegsInOrderOfImportance()) {
                        var trdLegBdr = trd.getSuitableBuilderFrom(trdLeg);
                        trdLegBdr
                                .tradeLegVersion(trdLeg.tradeLegVersion() + 1)
                                .tradeEventType(TradeEventType.CANCEL)
                                .tradeEventAction(TradeEventAction.ADD)
                        ;
                    }
                });

        // 3. Create rebooked Trade
        var newTrdEventAndAction = trdAmndCtx.tradeEventActionPair();
        TradeBuilder newTrdBdr = createBuilderFrom(trd.trade());
        newTrdBdr
                .tradeID(newTrdId)
                .tradeVersion(newTrdVer)
                .tradeLinks(List.of(new TradeLink(PARENT_TRADE.name, null, 0, 0, trd.tradeId(), canTrdVer)))
                .tradeEventType(newTrdEventAndAction.event())
                .tradeEventAction(newTrdEventAndAction.action())
        ;

        // 4. Reset nextTradeLegId provider so that rebooked trade will have TradeLegs with ids starting from 1
        trd.resetTradeLegIdProvider();

        // 5. Create rebooked TradeLegs only for those trade legs present in trdAmndCtx which are explicitly selected by implementation class to be valid for rebooked trade
        var trdLegBuildItems = buildTradeLegAmendment(trdAmndCtx, trd);
    }

    private List<BuildItem<>> buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, TradeLegAmendmentContextLazy trdLegAmndCtxLazy, T trd) {
        List<BuildItem<>> buildItems = new ArrayList<>();

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
                                var callback = trdLegAmndCtxEager.callback();
                                var runnableAfterCallback = () -> applyFutureAmendmentInclusion(trd);
                                var buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, trd);

                                var bi = new BuildItem<>(callback, runnableAfterCallback, buildStep);
                                buildItems.add(bi);
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
                                var callback = trdLegAmndCtxEager.callback();
                                var runnableAfterCallback = () -> applyFutureAmendmentInclusion(trd);
                                var buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, trd);

                                var bi = new BuildItem<>(callback, runnableAfterCallback, buildStep);
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
            var callback = trdLegAmndCtxLazy.callbackProvider().apply(tradeLeg);

            // Create the amendment subject context
            var trdLegAmndCtxEager = new TradeLegAmendmentContextEager(tradeLeg.tradeLegType(), tradeLeg, callback, concreteAmendableFields);
            // Register the build step with the templateBuilder
            var runnableAfterCallback = () -> applyFutureAmendmentInclusion(trd);
            var buildStep = () -> buildTradeLegAmendment(trdAmndCtx, trdLegAmndCtxEager, trd);

            var bi = new BuildItem<>(trdLegAmndCtxEager.callback(), runnableAfterCallback, buildStep);
            buildItems.add(bi);
        }

        return buildItems;
    }

    private TradeLegBuilder buildTradeLegAmendment(TradeAmendmentContext trdAmndCtx, TradeLegAmendmentContextEager trdLegAmndCtx, T trd) {
        TradeEventActionPair nextEventAndAction = trdAmndCtx.tradeEventActionPair();
        Set<AmendableField> amendableFields = trdLegAmndCtx.amendableFields();
        TradeLeg tradeLeg = trdLegAmndCtx.tradeLeg();
        // Create builder for amending cashMessage from the cashMessage being amended
        TradeLegBuilder amndBdr = trd.getSuitableBuilderFrom(tradeLeg);

        // If trade is rebooked, get a new tradeLegId. Note: the trd::nextTradeLegId counter was already reset in a previous step due to rebook event
        if (nextEventAndAction.event() == TradeEventType.REBOOK) {
            amndBdr
                    .tradeLegId(trd.nextTradeLegId())
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

    private void applyFutureAmendmentInclusion(T trdCtx) {
        if (amendmentCandidateSelectionCriteria().test(trdCtx)) {
            msgStoreHelper().storeMessageDataForFutureRndmRetrievalDay(trdCtx);
        }
    }
}
