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

import static io.alw.css.domain.cashflow.TradeEventAction.*;
import static io.alw.css.domain.cashflow.TradeEventType.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.tradeLink_counterSide;

public final class FxTemplate extends CashMessageTemplateWithDataStore {
    private long counterSideCashflowId;
    private final static Predicate<FoCashMessage> inclusionCriteria = msg -> msg.tradeEventType() != TradeEventType.CANCEL;

    public FxTemplate(Entity entity, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMessageTemplateProperties) {
        super(entity, TradeType.FX, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMessageTemplateProperties);
    }

    @Override
    public List<FoCashMessage> get() {
        // Get cash messages that need to be amended
        final List<FoCashMessage> messagesToBeAmended = msgStoreHelper.getMessagesToBeAmended();

        // Build amended cashMessages and cashMessages for a new FX trade. There are 2 cashMessages for a single FX trade
        List<FoCashMessage> newAndAmendedMsgs = ((FxTemplate) newTemplateBuilder())
                .withAmendedMessagesOf(messagesToBeAmended)
                .withRelatedTemplate(this::buildCounterSide)
                .withCustomTemplateValues()
                .buildWithRelatedTemplates();

        // Select new cash messages for future amendments and add to the message store
        msgStoreHelper.rndmlySelectValidAmendCandidatesAndSave(newAndAmendedMsgs, inclusionCriteria);

        return newAndAmendedMsgs;
    }

    /// Builds the counter side of the given FX message
    private FoCashMessage buildCounterSide(FoCashMessage fx1) {
        String counterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(fx1.counterpartyCode());
        Entity entity = refDataService.entityOtherThan(rndm, fx1.entityCode());
        String entityCode = entity.entityCode();
        String currCode = entity.currCode();

        FoCashMessageBuilder fx2Bdr = createBuilderFrom(fx1)
                // Values that differ for counter side of the FX deal
                .cashflowID(counterSideCashflowId)
                .counterpartyCode(counterpartyCode)
                .entityCode(entityCode)
                .currCode(currCode)
                .tradeLinks(List.of(new TradeLink(tradeLink_counterSide, String.valueOf(fx1.cashflowID()))))
                .payOrReceive(fx1.payOrReceive() == PayOrReceive.RECEIVE ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036))); // TODO and NOTE: The amount of the other side of FX trade is not calculated based on rate. It is just a random number which is incorrect.
        // bookCode and counterBookCode are not changed as they are dummy values as of now

        return fx2Bdr.build();
    }

    @Override
    public TemplateBuilder<FoCashMessage> withCustomTemplateValues() {
        IdProvider idProvider = IdProvider.singleton();
        // Create the builder with base values
        FoCashMessageBuilder bdr = getFoCashMsgBuilderForNewTemplate();
        // Generate cashflowID for the counter side of this FX deal
        counterSideCashflowId = idProvider.nextCashflowId();
        // Set the values specific to FX trade
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(50))
                .tradeLinks(List.of(new TradeLink(tradeLink_counterSide, String.valueOf(counterSideCashflowId))))
                .payOrReceive(rndm.nextBoolean() ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036)))
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