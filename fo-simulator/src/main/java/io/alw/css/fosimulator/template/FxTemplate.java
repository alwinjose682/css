package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.common.CashflowIds;
import io.alw.css.fosimulator.template.common.FxCashMessageContext;
import io.alw.datagen.template.TemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.cashflow.TradeEventAction.*;
import static io.alw.css.domain.cashflow.TradeEventType.*;
import static io.alw.css.fosimulator.model.TradeLinkConstants.tradeLink_counterSide;

public final class FxTemplate extends CashMessageTemplateWithDataStore<FxCashMessageContext> {
    // Message Store and Related
    private final CashMessageStoreHelper<FxCashMessageContext> msgStoreHelper;
    private final Predicate<FxCashMessageContext> amendableMsgSelectionCriteria = msg -> msg.tradeEventType() != TradeEventType.CANCEL
            && (msg.cashflowVersion() + msg.tradeVersion() <= msgTemplateHelper.cashMsgTemplateProps.maxPermittedAmendments() && msg.cashflowID() % 10 + msg.tradeID() % 10 > 10 /* To choose random cashflows*/);

    public FxTemplate(Entity entity, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMessageTemplateProperties) {
        super(entity, TradeType.FX, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMessageTemplateProperties);

        CashMessageStore<FxCashMessageContext> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    @Override
    protected FxTemplate templateBuildSteps() {
        // Build amended cashMessages and cashMessages for a new FX trade. There are 2 cashMessages for a single FX trade
        ((FxTemplate) newTemplateBuilder())
                .withMessageAmendments()
                .withTemplateValues();

        return this;
    }

    /// Build side 1 of the fx message
    @Override
    public TemplateBuilder<FxCashMessageContext> withTemplateValues() {
        // Create Ids and MessageContext for FX-Side-1
        var fxSide1Ids = CashMessageTemplateHelper.getIdsForVersionOneCashflowAndVersionOneTrade();
        var fxSide1Ctx = new FxCashMessageContext();
        // Create FoCashMessage builder for new template with default base values
        FoCashMessageBuilder bdr = getNewCashMsgBuilder(fxSide1Ids, fxSide1Ctx);
        // Generate trade IDs for the counter side of the FX deal
        var counterSideIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowFromExistingTrade(fxSide1Ids);
        // Set the values specific to the FX trade being built
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(50))
                .tradeLinks(List.of(
                        TradeLinkBuilder.builder()
                                .linkType(tradeLink_counterSide)
                                .relatedReference(null)
                                .relatedFoCashflowID(counterSideIds.cashflowID())
                                .relatedFoCashflowVersion(counterSideIds.cashflowVersion())
                                .relatedTradeID(counterSideIds.tradeID())
                                .relatedTradeVersion(counterSideIds.tradeVersion())
                                .build()))
                .payOrReceive(rndm.nextBoolean() ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036)))
        ;

        this.withRelatedObjectBuilder((fxSide1CtxParam) -> buildCounterSide(fxSide1CtxParam, counterSideIds));

        return this;
    }

    /// Builds the counter side(side 2) of the fx message
    private FxCashMessageContext buildCounterSide(FxCashMessageContext fxSide1Ctx, CashflowIds ids) {
        var fxSide1Msg = fxSide1Ctx.foCashMessage();
        String counterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(fxSide1Msg.counterpartyCode());
        Entity entity = refDataService.entityOtherThan(rndm, fxSide1Msg.entityCode());
        String entityCode = entity.entityCode();
        String currCode = entity.currCode();

        FoCashMessageBuilder fx2Bdr = createBuilderFrom(fxSide1Msg)
                // Id and version of fxSide2 already determined when fxSide1 was created
                .cashflowID(ids.cashflowID())
                .cashflowVersion(ids.cashflowVersion())
                .tradeID(ids.tradeID())
                .tradeVersion(ids.tradeVersion())
                // Values that differ for counter side of the FX deal
                .counterpartyCode(counterpartyCode)
                .entityCode(entityCode)
                .currCode(currCode)
                .tradeLinks(List.of(
                        TradeLinkBuilder.builder()
                                .linkType(tradeLink_counterSide)
                                .relatedReference(null)
                                .relatedFoCashflowID(fxSide1Msg.cashflowID())
                                .relatedFoCashflowVersion(fxSide1Msg.cashflowVersion())
                                .relatedTradeID(fxSide1Msg.tradeID())
                                .relatedTradeVersion(fxSide1Msg.tradeVersion())
                                .build()))
                .payOrReceive(fxSide1Msg.payOrReceive() == PayOrReceive.RECEIVE ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036))); // TODO and NOTE: The amount of the other side of FX trade is not calculated based on rate. It is just a random number which is incorrect.
        // bookCode and counterBookCode are not changed as they are dummy values as of now

        // Create context for fx-side-2
        var fxSide2Ctx = new FxCashMessageContext();
        fxSide2Ctx.setFoCashMessage(fx2Bdr.build());
        return fxSide2Ctx;
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

    @Override
    protected CashMessageStoreHelper<FxCashMessageContext> msgStoreHelper() {
        return msgStoreHelper;
    }

    @Override
    protected Predicate<FxCashMessageContext> amendableMsgSelectionCriteria() {
        return amendableMsgSelectionCriteria;
    }
}