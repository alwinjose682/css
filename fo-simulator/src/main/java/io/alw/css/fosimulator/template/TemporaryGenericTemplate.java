package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
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
public final class TemporaryGenericTemplate extends CashMessageTemplateWithDataStore<FoCashMessage> {
    private final CashMessageStoreHelper<FoCashMessage> msgStoreHelper;
    private final Predicate<FoCashMessage> amendableMsgSelectionCriteria = msg -> msg.tradeEventType() != TradeEventType.CANCEL
            && (msg.cashflowVersion() + msg.tradeVersion() <= msgTemplateHelper.cashMsgTemplateProps.maxPermittedAmendments() && msg.cashflowID() % 10 + msg.tradeID() % 10 > 10 /* To choose random cashflows*/);

    public TemporaryGenericTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMessageTemplateProperties) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMessageTemplateProperties);

        CashMessageStore<FoCashMessage> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    @Override
    protected TemporaryGenericTemplate templateBuildSteps() {
        ((TemporaryGenericTemplate) newTemplateBuilder())
                .withMessageAmendments()
                .withTemplateValues();

        return this;
    }

    /// Returns the same value that is passed to this method because, unlike other templates like [MmTemplate], this template does not need to store a MessageContext instead of just a list of FoCashMessage
    @Override
    protected List<FoCashMessage> mapToMessageContext(List<FoCashMessage> cashMessages) {
        return cashMessages;
    }

    /// Returns the same value that is passed to this method because, unlike other templates like [MmTemplate], this template does not need to store a MessageContext instead of just a list of FoCashMessage
    @Override
    protected List<FoCashMessage> mapToCashMessage(List<FoCashMessage> messageContext) {
        return messageContext;
    }

    @Override
    protected CashMessageStoreHelper<FoCashMessage> msgStoreHelper() {
        return msgStoreHelper;
    }

    @Override
    protected Predicate<FoCashMessage> amendableMsgSelectionCriteria() {
        return amendableMsgSelectionCriteria;
    }

    @Override
    public TemplateBuilder<FoCashMessage> withTemplateValues() {
        // Create the builder with base values
        FoCashMessageBuilder bdr = getNewFoCashMsgBuilder();
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
