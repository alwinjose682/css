package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.datagen.provider.CyclicStringDataProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

import static io.alw.css.fosimulator.model.AmendableFoCashMessageFields.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.tradeLink_parentCashflow;

sealed abstract class CashMessageTemplateWithDataStore
        extends CashMessageTemplate
        permits FxTemplate, TemporaryGenericTemplate {

    // Message Store and Related
    protected final CashMessageStoreHelper msgStoreHelper;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore
    private static final int maxAmendmentGenerationDelayInDays_relatedVal = 5;

    // Others
    private final CyclicStringDataProvider cyclicAmendableFieldsProvider;

    public CashMessageTemplateWithDataStore(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
        CashMessageStore msgStore = new InMemoryCashMessageStore();
        this.msgStoreHelper = new CashMessageStoreHelper(dayTicker.firstDay(), maxAmendmentGenerationDelayInDays, maxAmendmentGenerationDelayInDays_relatedVal, msgStore, rndm, msgTemplateHelper, cashMsgTemplateProps);
        this.cyclicAmendableFieldsProvider = new CyclicStringDataProvider(List.of(VALUE_DATE, AMOUNT, COUNTERPARTY_CODE));
    }

    protected CashMessageTemplate withAmendedMessagesOf(List<FoCashMessage> messagesToBeAmended) {
        for (FoCashMessage msg : messagesToBeAmended) {
            switch (cyclicAmendableFieldsProvider.next()) {
                case VALUE_DATE -> this.withRelatedTemplate(this::buildAmendedMessageForValueDate, msg);
                case AMOUNT -> this.withRelatedTemplate(this::buildAmendedMessageForAmount, msg);
                case COUNTERPARTY_CODE -> this.withRelatedTemplate(this::buildAmendedMessageForCounterparty, msg);
            }
        }
        return this;
    }

    private FoCashMessage buildAmendedMessageForCounterparty(FoCashMessage msg) {
        // NOTE: Here, it is required to get a counterpartyCode that is not used by 1) the current cashMessage being amended and 2) the counter side cashMessage of the current cashMessage
        // But, counterpartyCode of point 2 above is not available handy and hence there is a risk that the counterpartyCode used by counter side cashMessage may be re-used.
        String newCounterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(msg.counterpartyCode());
        return getBuilderWithDefaultAmdntBaseFrom(msg)
                .counterpartyCode(newCounterpartyCode)
                .build();
    }

    private FoCashMessage buildAmendedMessageForAmount(FoCashMessage msg) {
        return getBuilderWithDefaultAmdntBaseFrom(msg)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 75036)))
                .build();
    }

    private FoCashMessage buildAmendedMessageForValueDate(FoCashMessage msg) {
        return getBuilderWithDefaultAmdntBaseFrom(msg)
                .valueDate(msgTemplateHelper.getRndmValueDate())
                .build();
    }

    /// If NOT rebooked, then, increments the cashflow version and randomly chooses to increment the trade version
    ///
    /// If rebooked, then:
    ///
    /// 1) create a new trade with a new cashflow. The trade event is 'TradeEventType.REBOOK' and not 'TradeEventType.NEW_TRADE'
    /// 2) create cashflow to cancel the original cashflow. The trade event is 'TradeEventType.REBOOK'
    private FoCashMessageBuilder getBuilderWithDefaultAmdntBaseFrom(FoCashMessage msg) {
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
            // 1. Create new trade and cashflow IDs
            IdProvider idProvider = IdProvider.singleton();
            final long newTradeID = idProvider.nextTradeId();
            final long newCashflowID = idProvider.nextCashflowId();

            // 2. Create cancellation for the original cashflow and register in the TemplateBuilder
            this.withRelatedTemplate(origMsg -> {
                        List<TradeLink> newTradeLinks = origMsg.tradeLinks() != null && !origMsg.tradeLinks().isEmpty() ? new ArrayList<>(origMsg.tradeLinks()) : new ArrayList<>();
                        newTradeLinks.add(new TradeLink(tradeLink_childTrade, String.valueOf(newTradeID)));
                        newTradeLinks.add(new TradeLink(tradeLink_childCashflow, String.valueOf(newCashflowID)));
                        return createBuilderFrom(origMsg)
                                // Id Version
                                .tradeVersion(origMsg.tradeVersion())
                                .cashflowVersion(origMsg.cashflowVersion() + 1)
                                // Trade Event and Action
                                .tradeEventType(TradeEventType.CANCEL)
                                .tradeEventAction(TradeEventAction.ADD)
                                .tradeLinks(Collections.unmodifiableList(newTradeLinks))
                                .build();
                    }
                    , msg);

            // 3. Create the new trade and cashflow
            List<TradeLink> newTradeLinks = msg.tradeLinks() != null && !msg.tradeLinks().isEmpty() ? new ArrayList<>(msg.tradeLinks()) : new ArrayList<>();
            newTradeLinks.add(new TradeLink(tradeLink_parentTrade, String.valueOf(msg.tradeID())));
            newTradeLinks.add(new TradeLink(tradeLink_parentCashflow, String.valueOf(msg.cashflowID())));
            amndBdr
                    // Id Version
                    .tradeID(newTradeID)
                    .tradeVersion(1)
                    .cashflowID(newCashflowID)
                    .cashflowVersion(1)
                    .tradeLinks(Collections.unmodifiableList(newTradeLinks));
        }

        return amndBdr
                // Trade Event and Action
                .tradeEventType(nextEventAndAction.event())
                .tradeEventAction(nextEventAndAction.action());
    }
}
