package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.common.MmTemplateType;
import io.alw.datagen.template.TemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public final class MmTemplate extends CashMessageTemplateWithDataStore {
    private final static Predicate<FoCashMessage> amendableMsgCriteria = ;
    private MmTemplateType templateType;

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    @Override
    public List<FoCashMessage> get() {
        // Get cash messages that need to be amended
        final List<FoCashMessage> messagesToBeAmended = msgStoreHelper.getMessagesToBeAmended();

        // Build amended cashMessages and cashMessages for a new MM trade. An MM Trade can have three type of cashflows: Principal, Interest and Maturity
        List<FoCashMessage> newAndAmendedMsgs = ((MmTemplate) newTemplateBuilder())
                .withAmendedMessagesOf(messagesToBeAmended)
                .withRelatedTemplate()
                .withTemplateValues()
                .buildWithRelatedTemplates();

        // Select new cash messages for future amendments and add to the message store
        msgStoreHelper.selectAmendCandidatesAndSave(newAndAmendedMsgs, amendableMsgCriteria);

        return newAndAmendedMsgs;
    }

    @Override
    protected TradeEventActionPair getNextEventActionPair(TradeEventType amendMsgEvt, TradeEventAction amendMsgAct) {

    }

    @Override
    public TemplateBuilder<FoCashMessage> withTemplateValues() {
        // Create builder with base values for PRINCIPAL leg of the MoneyMarket trade
        FoCashMessageBuilder bdr = getFoCashMsgBuilderForNewTemplate();
        // Set values specific to the PRINCIPAL leg of the MM cashflow
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .tradeLinks()
                .payOrReceive(rndm.nextBoolean())
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036)))
        ;
    }
}
