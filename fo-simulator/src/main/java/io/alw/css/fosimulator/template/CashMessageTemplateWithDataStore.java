package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.common.CashMessageAmendmentContext;
import io.alw.css.fosimulator.template.common.MessageContext;
import io.alw.datagen.provider.AbstractCyclicDataProvider;
import io.alw.datagen.provider.CyclicDataProvider;
import io.alw.datagen.provider.CyclicStringDataProvider;
import io.alw.datagen.template.AggregateTemplateBuilderResult;

import java.math.BigDecimal;
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
                        .withTemplateValues()
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

    protected void buildAmendedMessage(CashMessageAmendmentContext amndCtx) {
        var amendableFields = amndCtx.amendableFields();
        var msg = amndCtx.msg();
        var callback = amndCtx.callback();
        var tradeEventActionPair = amndCtx.tradeEventActionPair();

        this.withRelatedItem(() -> {
                    var bdr = getBuilderWithDefaultAmdntBaseFrom(msg, tradeEventActionPair, callback);
                    for (AmendableFoCashMessageField amendableField : amendableFields) {
                        switch (amendableField) {
                            case ValueDate(var newValueDate) -> bdr.valueDate(newValueDate);
                            case Amount(var newAmount) -> bdr.amount(newAmount);
                            case CounterpartyCode(var newCounterpartyCode) -> bdr.counterpartyCode(newCounterpartyCode);
                            default -> throw new RuntimeException("Unknown amendable field");
                        }
                    }

                    return bdr;
                },
                callback);
    }

    /// If NOT rebooked, then, increments the cashflow version and randomly chooses to increment the trade version
    ///
    /// If rebooked, then:
    ///
    /// 1) create a new trade with a new cashflow. The trade event is 'TradeEventType.REBOOK' and not 'TradeEventType.NEW_TRADE'
    /// 2) create cashflow to cancel the original cashflow. The trade event is 'TradeEventType.REBOOK'
    private FoCashMessageBuilder getBuilderWithDefaultAmdntBaseFrom(FoCashMessage msg, TradeEventActionPair nextEventAndAction, Consumer<FoCashMessage> callback) {
        FoCashMessageBuilder amndBdr = createBuilderFrom(msg);

        // If NOT rebooked
        if (nextEventAndAction.event() != TradeEventType.REBOOK) {
            boolean incrementTradeVersion = rndm.nextInt(0, 100) > 70;
            amndBdr
                    // Id Version
                    .tradeVersion(incrementTradeVersion ? msg.tradeVersion() + 1 : msg.tradeVersion())
                    .cashflowVersion(msg.cashflowVersion() + 1);
        }
        // If rebooked
        else {
            // 1. Create new trade ID and cashflow ID
            IdProvider idProvider = IdProvider.singleton();
            final long rebookedTradeID = idProvider.nextTradeId();
            final int rebookedTradeVersion = VERSION_ONE;
            final long rebookedCashflowID = idProvider.nextCashflowId();
            final int rebookedCashflowVersion = VERSION_ONE;
            final int cancelledCashflowVersion = msg.cashflowVersion() + 1;
            final int cancelledTradeVersion = msg.tradeVersion();

            // 2. Create cancellation for the original cashflow and register in the TemplateBuilder
            this.withRelatedItem(() -> {

                        // Add trade link that corresponds to rebooked cashflow to the existing list of trade links
                        List<TradeLink> newTradeLinks = msg.tradeLinks() != null && !msg.tradeLinks().isEmpty() ? new ArrayList<>(msg.tradeLinks()) : new ArrayList<>();
                        newTradeLinks.add(TradeLinkBuilder.builder()
                                .linkType(childCashflow)
                                .relatedReference(null)
                                .relatedFoCashflowID(rebookedCashflowID)
                                .relatedFoCashflowVersion(rebookedCashflowVersion)
                                .relatedTradeID(rebookedTradeID)
                                .relatedTradeVersion(rebookedTradeVersion)
                                .build());

                        // Create builder for cashflow cancellation
                        return createBuilderFrom(msg)
                                // Id Version
                                .tradeVersion(cancelledTradeVersion)
                                .cashflowVersion(cancelledCashflowVersion)
                                // Trade Event and Action
                                .tradeEventType(TradeEventType.CANCEL)
                                .tradeEventAction(TradeEventAction.ADD)
                                .tradeLinks(Collections.unmodifiableList(newTradeLinks));
                    },
                    callback);

            // 3. Create the new trade and cashflow (because of trade rebook)
            List<TradeLink> newTradeLinks = msg.tradeLinks() != null && !msg.tradeLinks().isEmpty() ? new ArrayList<>(msg.tradeLinks()) : new ArrayList<>();
            // Add trade link of cancelled cashflow to the existing list of trade links
            newTradeLinks.add(TradeLinkBuilder.builder()
                    .linkType(parentCashflow)
                    .relatedReference(null)
                    .relatedFoCashflowID(msg.cashflowID())
                    .relatedFoCashflowVersion(cancelledCashflowVersion)
                    .relatedTradeID(msg.tradeID())
                    .relatedTradeVersion(cancelledTradeVersion)
                    .build());
            amndBdr
                    // Id Version
                    .tradeID(rebookedTradeID)
                    .tradeVersion(rebookedTradeVersion)
                    .cashflowID(rebookedCashflowID)
                    .cashflowVersion(rebookedCashflowVersion)
                    .tradeLinks(Collections.unmodifiableList(newTradeLinks));
        }

        return amndBdr
                // Trade Event and Action
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());
    }
}
