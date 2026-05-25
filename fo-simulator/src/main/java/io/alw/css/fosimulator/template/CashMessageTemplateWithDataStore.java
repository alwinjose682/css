package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.common.MessageContext;
import io.alw.datagen.provider.CyclicStringDataProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static io.alw.css.fosimulator.model.AmendableFoCashMessageFields.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.tradeLink_parentCashflow;

/// The type parameter M stands for MessageContext which is a combination of [FoCashMessage] and its metadata created by the implementations of this class.
/// Some implementations choose to store MessageContext instead of just FoCashMessage in [io.alw.css.fosimulator.store.CashMessageStore]
sealed abstract class CashMessageTemplateWithDataStore<M extends MessageContext>
        extends CashMessageTemplate<M>
        permits FxTemplate, MmTemplate, TemporaryGenericTemplate {
    private final CyclicStringDataProvider cyclicAmendableFieldsProvider;

    public CashMessageTemplateWithDataStore(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
        this.cyclicAmendableFieldsProvider = new CyclicStringDataProvider(List.of(VALUE_DATE, AMOUNT, COUNTERPARTY_CODE));
    }

    /// Implementation to be provided by child classes
    protected abstract CashMessageTemplateWithDataStore<M> templateBuildSteps();

    protected abstract CashMessageStoreHelper<M> msgStoreHelper();

    protected abstract Predicate<M> amendableMsgSelectionCriteria();

    /// Returns messages(new+amends) which can be consumed by the CashflowGenerators
    @Override
    public List<FoCashMessage> get() {
        // Build the cash messages(newly created + amendments)
        List<M> msgCtxs = this
                .templateBuildSteps()
                .build();

        // Select new cash messages for future amendments and add to the message store
        List<M> amendableCashMsgs = msgCtxs.stream().filter(amendableMsgSelectionCriteria()).toList();
        msgStoreHelper().storeMessageDataForAmendment(amendableCashMsgs);

        // Return messages(new+amends) which can be consumed by the CashflowGenerators
        return msgCtx.mapToCashMessage(msgCtxs);
    }

    protected CashMessageTemplateWithDataStore<M> withMessageAmendments() {
        // Get cash message data that need to be amended
        final List<M> messageContextsForAmendment = msgStoreHelper().retrieveMessageDataForAmendment();
        // Add the list of foCashMessages for amendment to the template build step
        this.withAmendedMessagesOf(messageContextsForAmendment);

        return this;
    }

    protected CashMessageTemplate<M> withAmendedMessagesOf(List<M> msgContextsForAmendment) {
        for (M msgContext : msgContextsForAmendment) {
//            var msg = msgContext.foCashMessage();
            switch (cyclicAmendableFieldsProvider.next()) {
                case VALUE_DATE -> this.withRelatedObjectBuilder(this::buildAmendedMessageForValueDate, msgContext);
                case AMOUNT -> this.withRelatedObjectBuilder(this::buildAmendedMessageForAmount, msgContext);
                case COUNTERPARTY_CODE -> this.withRelatedObjectBuilder(this::buildAmendedMessageForCounterparty, msgContext);
            }
        }
        return this;
    }

    private M buildAmendedMessageForCounterparty(M msgCtx) {
        // NOTE: Here, it is required to get a counterpartyCode that is not used by 1) the current cashMessage being amended and 2) the counter side cashMessage of the current cashMessage
        // But, counterpartyCode of point 2 above is not available handy and hence there is a risk that the counterpartyCode used by counter side cashMessage may be re-used.
        String newCounterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(msgCtx.foCashMessage().counterpartyCode());
        FoCashMessage amendedMsg = getBuilderWithDefaultAmdntBaseFrom(msgCtx)
                .counterpartyCode(newCounterpartyCode)
                .build();

        // return new messageContext with amended message
        return msgCtx.with(amendedMsg);
    }

    private M buildAmendedMessageForAmount(M msgCtx) {
        var amendedMsg = getBuilderWithDefaultAmdntBaseFrom(msgCtx)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 75036)))
                .build();

        // return new messageContext with amended message
        return msgCtx.with(amendedMsg);
    }

    private M buildAmendedMessageForValueDate(M msgCtx) {
        var amendedMsg = getBuilderWithDefaultAmdntBaseFrom(msgCtx)
                .valueDate(msgTemplateHelper.getRndmValueDate())
                .build();

        // return new messageContext with amended message
        return msgCtx.with(amendedMsg);
    }

    /// If NOT rebooked, then, increments the cashflow version and randomly chooses to increment the trade version
    ///
    /// If rebooked, then:
    ///
    /// 1) create a new trade with a new cashflow. The trade event is 'TradeEventType.REBOOK' and not 'TradeEventType.NEW_TRADE'
    /// 2) create cashflow to cancel the original cashflow. The trade event is 'TradeEventType.REBOOK'
    private FoCashMessageBuilder getBuilderWithDefaultAmdntBaseFrom(M msgCtx) {
        var msg = msgCtx.foCashMessage();
        TradeEventActionPair nextEventAndAction = getNextEventActionPair(msg.tradeEventType(), msg.tradeEventAction());
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
            this.withRelatedObjectBuilder(() -> {

                // Add trade link that corresponds to rebooked cashflow to the existing list of trade links
                List<TradeLink> newTradeLinks = msg.tradeLinks() != null && !msg.tradeLinks().isEmpty() ? new ArrayList<>(msg.tradeLinks()) : new ArrayList<>();
                newTradeLinks.add(TradeLinkBuilder.builder()
                        .linkType(tradeLink_childCashflow)
                        .relatedReference(null)
                        .relatedFoCashflowID(rebookedCashflowID)
                        .relatedFoCashflowVersion(rebookedCashflowVersion)
                        .relatedTradeID(rebookedTradeID)
                        .relatedTradeVersion(rebookedTradeVersion)
                        .build());
                var cancelMsg = createBuilderFrom(msg)
                        // Id Version
                        .tradeVersion(cancelledTradeVersion)
                        .cashflowVersion(cancelledCashflowVersion)
                        // Trade Event and Action
                        .tradeEventType(TradeEventType.CANCEL)
                        .tradeEventAction(TradeEventAction.ADD)
                        .tradeLinks(Collections.unmodifiableList(newTradeLinks))
                        .build();

                return msgCtx.with(cancelMsg);
            });

            // 3. Create the new trade and cashflow (because of trade rebook)
            List<TradeLink> newTradeLinks = msg.tradeLinks() != null && !msg.tradeLinks().isEmpty() ? new ArrayList<>(msg.tradeLinks()) : new ArrayList<>();
            // Add trade link of cancelled cashflow to the existing list of trade links
            newTradeLinks.add(TradeLinkBuilder.builder()
                    .linkType(tradeLink_parentCashflow)
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
