package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.template.domain.CashLeg;
import io.alw.css.fosimulator.template.domain.TradeContext;
import io.alw.css.fosimulator.template.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.template.domain.CashLegType;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilderResult;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static io.alw.css.fosimulator.template.domain.CashLegType.*;

/// The type parameter M stands for MessageContext which is a combination of [FoCashMessage] and its metadata created by the implementations of this class.
/// Some implementations choose to store MessageContext instead of just FoCashMessage in [io.alw.css.fosimulator.store.CashMessageStore]
sealed abstract class CashMessageTemplateWithDataStore<M extends TradeContext>
        extends CashMessageTemplate<M>
        permits FxTemplate, MmTemplate, TemporaryGenericTemplate {

    public CashMessageTemplateWithDataStore(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    protected abstract void buildAmendedMessage(Consumer<CashMessageAmendmentContext> buildAmendedMessageFunc, M trdCtxForAmendment);

    protected abstract CashMessageStoreHelper<M> msgStoreHelper();

    protected abstract Predicate<M> amendableMsgSelectionCriteria();

    /// Return cash messages(new+amends) which can be consumed by the CashflowGenerators and published to CSS
    @Override
    public List<FoCashMessage> get() {
        // Build the cash messages(newly created + amendments)
        AggregateTemplateBuilderResult<M, FoCashMessage> buildResult =
                ((CashMessageTemplateWithDataStore<M>) newBuildCycle())
                        .withMessageAmendments()
                        .withRootTemplateValues()
                        .build();

        // Select cash messages from the build result which includes new+amends for future amendments
        List<M> amendableCashMsgs = buildResult.stream().filter(amendableMsgSelectionCriteria()).toList();
        // Add the future amend candidates to the message store
        msgStoreHelper().storeMessagesForFutureRndmRetrievalDay(amendableCashMsgs);

        // Return messages(new+amends) which can be consumed by the CashflowGenerators
        return sdsdsd.mapToCashMessage(buildResult);
    }

    protected CashMessageTemplateWithDataStore<M> withMessageAmendments() {
        // Get cash message data that need to be amended
        final List<M> trdCtxsForAmendment = msgStoreHelper().retrieveMessagesForCurrentDay();
        // Add the list of foCashMessages for amendment to the template build step
        for (M trdCtx : trdCtxsForAmendment) {
            buildAmendedMessage(this::buildAmendedMessage, trdCtx);
        }
        return this;
    }

    /// Adds the step to lazily build amended messages in the [io.alw.datagen.template.AggregateTemplateBuilder].
    /// The steps to lazily build(functions) and the actual build done by [io.alw.datagen.template.AggregateTemplateBuilder] are performed in FIFO order
    protected void buildAmendedMessage(CashMessageAmendmentContext amndCtx) {
        var amndSubCtxs = amndCtx.amendmentSubjectContexts();
        for (AmendmentSubjectContext amndSubCtx : amndSubCtxs) {
            switch (amndSubCtx) {
                case AmendmentSubjectContextEager subCtxEager -> this.withRelatedItem(subCtxEager.callback(), () -> buildAmendedMessageFor(amndCtx, subCtxEager));
                case AmendmentSubjectContextLazy subCtxLazy -> this.withRelatedItem(() -> buildAmendedMessageFor(amndCtx, subCtxLazy));
            }
        }
    }

    private void buildAmendedMessageFor(CashMessageAmendmentContext amndCtx, AmendmentSubjectContextLazy subCtxLazy) {
        for (AmendableFoCashMessageField amendableFieldSupplier : subCtxLazy.amendableFields()) {
            switch (amendableFieldSupplier) {
                case AmendableFoCashMessageFieldSupplier fieldSupplier -> {
                    switch (fieldSupplier) {
                        case AmendableFoCashMessageFieldSupplier.ConditionalSupplier supplier -> {
                            // 1. Evaluate the condition and if applicable proceed with generating the build step
                            var cashLeg = supplier.conditionSubject();
                            var condition = supplier.condition();
                            if (condition.test(cashLeg)) {
                                Set<AmendableFoCashMessageField> amendableFields = supplier
                                        .amendableFieldSupplierFunctions()
                                        .stream()
                                        .map(func -> func.apply(cashLeg))
                                        .collect(Collectors.toSet());

                                // 2. Create the amendment subject context with the above derived values
                                var amndSubCtxEager = new AmendmentSubjectContextEager(cashLeg, subCtxLazy.callbackProvider().apply(cashLeg), amendableFields);
                                // 4. Register the build step with the templateBuilder
                                this.withRelatedItem(amndSubCtxEager.callback(), () -> buildAmendedMessageFor(amndCtx, amndSubCtxEager));
                            }
                        }
                        case AmendableFoCashMessageFieldSupplier.SupplierWithMessageSelector supplier -> {
                            // 1. Get the amendment subjects
                            List<? extends CashLeg> cashLegs = supplier.amendmentSubjectSelector().apply(supplier.trdCtx());
                            // 2. Get the fields for amendment for each amendment subject. If there are no amendment subject, nothing is there to build
                            for (CashLeg cashLeg : cashLegs) {
                                Set<AmendableFoCashMessageField> amendableFields = supplier
                                        .amendableFieldSupplierFunctions()
                                        .stream()
                                        .map(func -> func.apply(cashLeg))
                                        .collect(Collectors.toSet());

                                // 3. Create the amendment subject context with the above derived values
                                var amndSubCtxEager = new AmendmentSubjectContextEager(cashLeg, subCtxLazy.callbackProvider().apply(cashLeg), amendableFields);
                                // 4. Register the build step with the templateBuilder
                                this.withRelatedItem(amndSubCtxEager.callback(), () -> buildAmendedMessageFor(amndCtx, amndSubCtxEager));
                            }
                        }
                    }
                }
                case AmendableFoCashMessageField.Amount _, AmendableFoCashMessageField.CounterpartyCode _, AmendableFoCashMessageField.ValueDate _ -> {
                    // NOTE: This type of usage as mentioned in the RuntimeException message is not possible to occur in any case due to the structure of AmendmentSubjectContextEager and AmendmentSubjectContextLazy
                    throw new RuntimeException("Incorrect use of `AmendableFoCashMessageField` type. Instead, `AmendableFoCashMessageFieldSupplier` type is expected to be used when the values are NOT known at the time of constructing the object");
                }
            }
        }
    }

    private FoCashMessageBuilder buildAmendedMessageFor(CashMessageAmendmentContext amndCtx, AmendmentSubjectContextEager amndSubjectCtx) {
        TradeEventActionPair nextEventAndAction = amndCtx.tradeEventActionPair();
        CashLeg amendmentSubjectLeg = amndSubjectCtx.amendmentSubject();
        FoCashMessage amendmentSubjectMsg = amendmentSubjectLeg.cashMessage();
        CashLegType amendmentSubjectLinkType = amendmentSubjectLeg.cashLegType();
        Set<AmendableFoCashMessageField> amendableFields = amndSubjectCtx.amendableFields();

        // Create builder for amending cashMessage from the cashMessage being amended
        FoCashMessageBuilder amndBdr = createBuilderFrom(amendmentSubjectLeg, amendmentSubjectLinkType);

        // If NOT rebooked, increments the cashflow version and randomly chooses to increment the trade version.
        // These same values used for the first message are used for the subsequent messages being amended that belong to the same MessageContext
        // The TradeEventType of the root and the related cashMessages being amended will be same. If not same, then Ids assigned to the amended cashflow will go wrong
        // (Ex: amending PRINCIPAL of an MM trade for TradeEventType#REBOOK, may result in amending MATURITY leg with the same TradeEventType as REBOOK)
        if (nextEventAndAction.event() != TradeEventType.REBOOK) {
            var rootAmendedMsgIds = amndCtx.computeFirstAmendedCashMessageIdsIfAbsent(amendmentSubjectMsg, (amndSubject) -> {
                boolean incrementTradeVersion = rndm.nextInt(0, 100) > 70;
                var amendedCashflowId = amndSubject.cashflowID();
                var amendedCashflowVersion = amndSubject.cashflowVersion() + 1;
                var amendedTradeId = amndSubject.tradeID();
                var amendedTradeVersion = incrementTradeVersion ? amndSubject.tradeVersion() + 1 : amndSubject.tradeVersion();

                return new Ids(null, amendedCashflowId, amendedCashflowVersion, amendedTradeId, amendedTradeVersion);
            });

            amndBdr
                    .tradeVersion(rootAmendedMsgIds.tradeVersion())
                    .cashflowVersion(amendmentSubjectMsg.cashflowVersion() + 1);
        }
        // If trade is rebooked, create a cancellation of the amendmentSubjectMsg in addition to the steps specific for rebook event
        else {
            handleTradeRebookEvent(amndCtx, amndBdr, amndSubjectCtx);
        }

        // Set new trade event and action
        amndBdr
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());

        for (AmendableFoCashMessageField amendableField : amendableFields) {
            switch (amendableField) {
                case AmendableFoCashMessageField.ValueDate(var newValueDate) -> amndBdr.valueDate(newValueDate);
                case AmendableFoCashMessageField.Amount(var newAmount) -> amndBdr.amount(newAmount);
                case AmendableFoCashMessageField.CounterpartyCode(var newCounterpartyCode) -> amndBdr.counterpartyCode(newCounterpartyCode);
                case AmendableFoCashMessageFieldSupplier _ -> {
                    // NOTE: This type of usage as mentioned in the RuntimeException message is not possible to occur in any case due to the structure of AmendmentSubjectContextEager and AmendmentSubjectContextLazy
                    throw new RuntimeException("Incorrect use of `AmendableFoCashMessageFieldSupplier` type. `AmendableFoCashMessageFieldSupplier` type should be used only when the values are NOT known at the time of constructing the object");
                }
            }
        }

        return amndBdr;
    }

    /// 1) creates a new trade with a new cashMessage.
    /// 2) creates cashflow to cancel the original cashMessage.
    private void handleTradeRebookEvent(CashMessageAmendmentContext rootAmendedMsgCtx, FoCashMessageBuilder amndBdr, AmendmentSubjectContextEager amndSubjectCtx) {
        CashLeg amendmentSubjectLeg = amndSubjectCtx.amendmentSubject();
        FoCashMessage amendmentSubjectMsg = amendmentSubjectLeg.cashMessage();
        CashLegType amendmentSubjectLinkType = amendmentSubjectLeg.cashLegType();
        Consumer<FoCashMessage> callback = amndSubjectCtx.callback();

        // 1. Create new trade ID and cashflow ID
        // These same values used for the first message are used for the subsequent messages being amended that belong to the same MessageContext
        var rebookedTradeIds = rootAmendedMsgCtx.computeFirstAmendedCashMessageIdsIfAbsent(amendmentSubjectMsg, (_) -> {
            IdProvider idProvider = IdProvider.singleton();
            final long rebookedCashflowID = idProvider.nextCashflowId();
            final long rebookedTradeID = idProvider.nextTradeId();
            return new Ids(null, rebookedCashflowID, VERSION_ONE, rebookedTradeID, VERSION_ONE);
        });

        final int cancelledCashflowVersion = amendmentSubjectMsg.cashflowVersion() + 1;
        final int cancelledTradeVersion = amendmentSubjectMsg.tradeVersion();

        // 2. Create cancellation for the original cashflow and register in the TemplateBuilder
        this.withRelatedItem(callback, () -> {
            // Add trade link that corresponds to rebooked cashflow to the existing list of trade links
            List<TradeLink> newTradeLinks = amendmentSubjectMsg.tradeLinks() != null && !amendmentSubjectMsg.tradeLinks().isEmpty() ? new ArrayList<>(amendmentSubjectMsg.tradeLinks()) : new ArrayList<>();
            newTradeLinks.add(TradeLinkBuilder.TradeLink(
                    CHILD_CASHFLOW.name, null,
                    rebookedTradeIds.cashflowID(), rebookedTradeIds.cashflowVersion(),
                    rebookedTradeIds.tradeID(), rebookedTradeIds.tradeVersion()));

            // Create builder for cashflow cancellation
            return createBuilderFrom(amendmentSubjectLeg, amendmentSubjectLinkType)
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
                amendmentSubjectMsg.cashflowID(), cancelledCashflowVersion,
                amendmentSubjectMsg.tradeID(), cancelledTradeVersion));
        // Add trade link of this new cashflow
        newTradeLinks.add(TradeLinkBuilder.TradeLink(
                amendmentSubjectLinkType.name, null,
                rebookedTradeIds.cashflowID(), rebookedTradeIds.cashflowVersion(),
                rebookedTradeIds.tradeID(), rebookedTradeIds.tradeVersion()));

        // Note: This amndBdr is used further in the caller method to set other fields like the amended field, trade event and action etc
        amndBdr
                // Id Version
                .tradeID(rebookedTradeIds.tradeID())
                .tradeVersion(rebookedTradeIds.tradeVersion())
                .cashflowID(rebookedTradeIds.cashflowID())
                .cashflowVersion(rebookedTradeIds.cashflowVersion())
                .tradeLinks(Collections.unmodifiableList(newTradeLinks));

    }
}
