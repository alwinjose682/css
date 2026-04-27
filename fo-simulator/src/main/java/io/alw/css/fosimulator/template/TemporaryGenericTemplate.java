package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.datagen.template.TemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.cashflow.TradeEventAction.ADD;
import static io.alw.css.domain.cashflow.TradeEventAction.MODIFY;
import static io.alw.css.domain.cashflow.TradeEventAction.REMOVE;
import static io.alw.css.domain.cashflow.TradeEventType.*;
import static io.alw.css.domain.cashflow.TradeEventType.REBOOK;

/// Note: This is only a temporary template that is used only till the [TradeType] specific templates are written.
/// Currently, only FX trade has a proper template: [FxTemplate]
public final class TemporaryGenericTemplate extends CashMessageTemplateWithDataStore {
    private final static Predicate<FoCashMessage> inclusionCriteria = msg -> msg.tradeEventType() != TradeEventType.CANCEL;

    public TemporaryGenericTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMessageTemplateProperties) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMessageTemplateProperties);
    }

    @Override
    public List<FoCashMessage> get() {
        // Get cash messages that need to be amended
        final List<FoCashMessage> messagesToBeAmended = msgStoreHelper.getMessagesToBeAmended();

        // Build amended cashMessages and cashMessages for a new FX trade. There are 2 cashMessages for a single FX trade
        List<FoCashMessage> newAndAmendedMsgs = ((CashMessageTemplateWithDataStore) newTemplateBuilder())
                .withAmendedMessagesOf(messagesToBeAmended)
                .withCustomTemplateValues()
                .buildWithRelatedTemplates();

        // Select new cash messages for future amendments and add to the message store
        msgStoreHelper.rndmlySelectValidAmendCandidatesAndSave(newAndAmendedMsgs, inclusionCriteria);

        return newAndAmendedMsgs;
    }

    @Override
    public TemplateBuilder<FoCashMessage> withCustomTemplateValues() {
        // Create the builder with base values
        FoCashMessageBuilder bdr = getFoCashMsgBuilderForNewTemplate();
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(20))
                .tradeLinks(null)
                .payOrReceive(rndm.nextBoolean() ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 52458)))
        ;

        return this;
    }

    @Override
    protected TradeEventActionPair getNextEventActionPair(TradeEventType amendMsgEvt, TradeEventAction amendMsgAct) {
        int rndmNum = rndm.nextInt(1, 100);
        return switch (amendMsgEvt) {
            case NEW_TRADE -> {
                if (rndmNum > 40) yield new TradeEventActionPair(AMEND, ADD);
                else if (rndmNum > 10) yield new TradeEventActionPair(CANCEL, ADD);
                else yield new TradeEventActionPair(REBOOK, ADD);
            }
            case REBOOK -> {
                if (rndmNum > 10) yield new TradeEventActionPair(AMEND, ADD);
                else yield new TradeEventActionPair(CANCEL, ADD);
            }
            case AMEND -> {
                if (amendMsgAct == REMOVE) yield new TradeEventActionPair(AMEND, ADD);
                else if (rndmNum > 30) {
                    if (amendMsgAct == ADD) yield new TradeEventActionPair(AMEND, MODIFY);
                    else if (amendMsgAct == MODIFY) {
                        if (rndmNum > 60) yield new TradeEventActionPair(AMEND, MODIFY);
                        else yield new TradeEventActionPair(AMEND, REMOVE);
                    } else /*if (amendMsgAct == REMOVE)*/ yield new TradeEventActionPair(AMEND, ADD);
                } else if (rndmNum > 20) yield new TradeEventActionPair(CANCEL, ADD);
                else yield new TradeEventActionPair(REBOOK, ADD);
            }
            case CANCEL -> throw new RuntimeException("Attempt to amend a cancelled cashflow is invalid");

            default -> throw new IllegalStateException("Unexpected value: " + amendMsgEvt);
        };
    }
}
