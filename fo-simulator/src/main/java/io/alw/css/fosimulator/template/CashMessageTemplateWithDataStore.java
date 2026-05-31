package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.common.CashMessageAmendmentContext;
import io.alw.css.fosimulator.template.common.Ids;
import io.alw.css.fosimulator.template.common.MessageContext;
import io.alw.css.fosimulator.template.common.RootAmendedCashMessageContext;
import io.alw.datagen.template.AggregateTemplateBuilderResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static io.alw.css.fosimulator.model.AmendableFoCashMessageField.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.parentCashflow;

/// The type parameter M stands for MessageContext which is a combination of [FoCashMessage] and its metadata created by the implementations of this class.
/// Some implementations choose to store MessageContext instead of just FoCashMessage in [io.alw.css.fosimulator.store.CashMessageStore]
sealed abstract class CashMessageTemplateWithDataStore<M extends MessageContext>
        extends CashMessageTemplate<M>
        permits FxTemplate, MmTemplate, TemporaryGenericTemplate {

    public CashMessageTemplateWithDataStore(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    protected abstract void buildAmendedMessage(Consumer<CashMessageAmendmentContext> buildAmendedMessageFunc, List<M> msgCtxsForAmendment);

    protected abstract CashMessageStoreHelper<M> msgStoreHelper();

    protected abstract Predicate<M> amendableMsgSelectionCriteria();

    /// Returns messages(new+amends) which can be consumed by the CashflowGenerators
    @Override
    public List<FoCashMessage> get() {
        // Build the cash messages(newly created + amendments)
        AggregateTemplateBuilderResult<M, FoCashMessage> buildResult =
                ((CashMessageTemplateWithDataStore<M>) newTemplateBuilder())
                        .withMessageAmendments()
                        .withRootTemplateValues()
                        .build();

        // Select cash messages from the build result which includes new+amends for future amendments
        List<M> amendableCashMsgs = buildResult.stream().filter(amendableMsgSelectionCriteria()).toList();
        // Add the future amend candidates to the message store
        msgStoreHelper().storeMessagesForFutureRndmRetrievalDay(amendableCashMsgs);

        // Return messages(new+amends) which can be consumed by the CashflowGenerators
        return msgCtx.mapToCashMessage(buildResult);
    }

    protected CashMessageTemplateWithDataStore<M> withMessageAmendments() {
        // Get cash message data that need to be amended
        final List<M> msgCtxsForAmendment = msgStoreHelper().retrieveMessagesForCurrentDay();
        // Add the list of foCashMessages for amendment to the template build step
        buildAmendedMessage(this::buildAmendedMessage, msgCtxsForAmendment);

        return this;
    }

    /// TODO: "            // These same values used for the first message are used for the subsequent related messages being amended"
    /// TODO: The same values should be used for the messages that belong to the same group, ie; to the same [MessageContext].
    /// So a method in [CashMessageTemplateWithDataStore] should make sure that instead of just trusting the extending class to do the same.
    /// How to do it? once way is to let this class to provide provide the [CashMessageAmendmentContext] object when the child class provide the necessary params
    /// ex: change the parameter accepted by this method to Supplier<CashMessageAmendmentContext>
    protected void buildAmendedMessage(CashMessageAmendmentContext amndCtx) {
        var callback = amndCtx.callback();
        this.withRelatedItem(callback, () -> {
            var rootAmendedMsgCtx = amndCtx.rootAmendedMsgCtx();
            var amendableFields = rootAmendedMsgCtx.amendableFields();
            var nextEventAndAction = rootAmendedMsgCtx.tradeEventActionPair();
            var amendmentSubject = amndCtx.amendmentSubject();
            var amendmentSubjectLinkType = amndCtx.amendmentSubjectLinkType();

            // Create builder for amending cashMessage from the cashMessage being amended
            FoCashMessageBuilder amndBdr = createBuilderFrom(amendmentSubject, amendmentSubjectLinkType);

            // If NOT rebooked, increments the cashflow version and randomly chooses to increment the trade version.
            // These same values used for the first message are used for the subsequent messages being amended that belong to the same MessageContext
            // The TradeEventType of the root and the related cashMessages being amended will be same. If not same, then Ids assigned to the amended cashflow will go wrong
            // (Ex: amending PRINCIPAL of an MM trade for TradeEventType#REBOOK, may result in amending MATURITY leg with the same TradeEventType as REBOOK)
            if (nextEventAndAction.event() != TradeEventType.REBOOK) {
                var rootAmendedMsgIds = rootAmendedMsgCtx.computeFirstAmendedCashMessageIdsIfAbsent(amendmentSubject, (amndSubject) -> {
                    boolean incrementTradeVersion = rndm.nextInt(0, 100) > 70;
                    var amendedCashflowId = amndSubject.cashflowID();
                    var amendedCashflowVersion = amndSubject.cashflowVersion() + 1;
                    var amendedTradeId = amndSubject.tradeID();
                    var amendedTradeVersion = incrementTradeVersion ? amndSubject.tradeVersion() + 1 : amndSubject.tradeVersion();

                    return new Ids(null, amendedCashflowId, amendedCashflowVersion, amendedTradeId, amendedTradeVersion);
                });

                amndBdr
                        .tradeVersion(rootAmendedMsgIds.tradeVersion())
                        .cashflowVersion(amendmentSubject.cashflowVersion() + 1);
            }
            // If trade is rebooked, create a cancellation of the amendmentSubject in addition to the steps specific for rebook event
            else {
                handleTradeRebookEvent(rootAmendedMsgCtx, amendmentSubject, amendmentSubjectLinkType, amndBdr, callback);
            }

            // Set new trade event and action
            amndBdr
                    .tradeEventType(nextEventAndAction.event())
                    .tradeEventAction(nextEventAndAction.action());

            for (AmendableFoCashMessageField amendableField : amendableFields) {
                switch (amendableField) {
                    case ValueDate(var newValueDate) -> amndBdr.valueDate(newValueDate);
                    case Amount(var newAmount) -> amndBdr.amount(newAmount);
                    case CounterpartyCode(var newCounterpartyCode) -> amndBdr.counterpartyCode(newCounterpartyCode);
                    default -> throw new RuntimeException("Unknown amendable field");
                }
            }

            return amndBdr;
        });
    }

    /// 1) creates a new trade with a new cashMessage.
    /// 2) creates cashflow to cancel the original cashMessage.
    private void handleTradeRebookEvent(RootAmendedCashMessageContext rootAmendedMsgCtx, FoCashMessage amendmentSubject, String amendmentSubjectLinkType, FoCashMessageBuilder amndBdr, Consumer<FoCashMessage> callback) {
        // 1. Create new trade ID and cashflow ID
        // These same values used for the first message are used for the subsequent messages being amended that belong to the same MessageContext
        var rebookedTradeIds = rootAmendedMsgCtx.computeFirstAmendedCashMessageIdsIfAbsent(amendmentSubject, (_) -> {
            IdProvider idProvider = IdProvider.singleton();
            final long rebookedCashflowID = idProvider.nextCashflowId();
            final long rebookedTradeID = idProvider.nextTradeId();
            return new Ids(null, rebookedCashflowID, VERSION_ONE, rebookedTradeID, VERSION_ONE);
        });

        final int cancelledCashflowVersion = amendmentSubject.cashflowVersion() + 1;
        final int cancelledTradeVersion = amendmentSubject.tradeVersion();

        // 2. Create cancellation for the original cashflow and register in the TemplateBuilder
        this.withRelatedItem(callback, () -> {
            // Add trade link that corresponds to rebooked cashflow to the existing list of trade links
            List<TradeLink> newTradeLinks = amendmentSubject.tradeLinks() != null && !amendmentSubject.tradeLinks().isEmpty() ? new ArrayList<>(amendmentSubject.tradeLinks()) : new ArrayList<>();
            newTradeLinks.add(TradeLinkBuilder.TradeLink(
                    childCashflow, null,
                    rebookedTradeIds.cashflowID(), rebookedTradeIds.cashflowVersion(),
                    rebookedTradeIds.tradeID(), rebookedTradeIds.tradeVersion()));

            // Create builder for cashflow cancellation
            return createBuilderFrom(amendmentSubject, amendmentSubjectLinkType)
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
                parentCashflow, null,
                amendmentSubject.cashflowID(), cancelledCashflowVersion,
                amendmentSubject.tradeID(), cancelledTradeVersion));
        // Add trade link of this new cashflow
        newTradeLinks.add(TradeLinkBuilder.TradeLink(
                amendmentSubjectLinkType, null,
                rebookedTradeIds.cashflowID(), rebookedTradeIds.cashflowVersion(),
                rebookedTradeIds.tradeID(), rebookedTradeIds.tradeVersion()));
        amndBdr
                // Id Version
                .tradeID(rebookedTradeIds.tradeID())
                .tradeVersion(rebookedTradeIds.tradeVersion())
                .cashflowID(rebookedTradeIds.cashflowID())
                .cashflowVersion(rebookedTradeIds.cashflowVersion())
                .tradeLinks(Collections.unmodifiableList(newTradeLinks));

    }
}
