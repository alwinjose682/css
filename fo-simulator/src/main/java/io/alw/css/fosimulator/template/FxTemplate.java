package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.model.*;
import io.alw.css.fosimulator.template.domain.FxTradeContext;
import io.alw.datagen.provider.AbstractCyclicDataProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.fosimulator.template.domain.CashLegType.*;
import static io.alw.css.fosimulator.template.model.AmendableFoCashMessageFieldType.*;

public final class FxTemplate extends CashMessageAmendmentTemplate<FxTradeContext> {
    private final CashMessageStoreHelper<FxTradeContext> msgStoreHelper;
    private static final Supplier<Set<AmendableFoCashMessageFieldType>> cyclicAmendableFoCashMessageFieldTypeProvider = new CyclicAmendableFoCashMessageFieldProvider(getListOfAmendableCashMessageFieldTypes());

    public FxTemplate(Entity entity, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMessageTemplateProperties) {
        super(entity, TradeType.FX, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMessageTemplateProperties);

        CashMessageStore<FxTradeContext> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    @Override
    protected Predicate<FxTradeContext> tradeContextAmendmentFrequency() {
        return _ -> rndm.nextInt(0, 100) > 80;
    }

    /// Build side 1 of the fx message
    @Override
    public FxTemplate withRootTemplateValues() {
        // Create Ids for FX-Side-1 and Fx-Side-2
        var fxSide1Ids = CashMessageTemplateHelper.getNewTradeIds(FX_SIDE1);
        var fxSide2Ids = CashMessageTemplateHelper.getNewCashMsgIdsFromExistingTrade(FX_SIDE2, fxSide1Ids);
        // Create message context and all tradeLinks
        var trdCtx = new FxTradeContext(TradeType.FX);
        // Create FoCashMessage builder for new template with default base values
        FoCashMessageBuilder bdr = getBaseCashMsgBuilder(fxSide1Ids, trdCtx);
        // Set the values specific to the FX trade being built
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(50))
                .payOrReceive(rndm.nextBoolean() ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036)))
        ;

        this.withGroupedItem(trdCtx::setSide2Msg, () -> buildFxSide2(trdCtx, fxSide2Ids));

        return this;
    }

    /// Builds the counter side(side 2) of the fx message
    private FoCashMessageBuilder buildFxSide2(FxTradeContext trdCtx, Ids ids) {
        var fxSide1Msg = trdCtx.side1Msg();
        String counterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(fxSide1Msg.counterpartyCode());
        Entity entity = refDataService.entityOtherThan(rndm, fxSide1Msg.entityCode());
        String entityCode = entity.entityCode();
        String currCode = entity.currCode();

        return createBuilderFrom(fxSide1Msg, trdCtx.rootFoCashMessage(), FX_SIDE2)
                // Id and version of fxSide2 was already determined when fxSide1 was created
                .cashflowID(ids.cashflowID())
                .cashflowVersion(ids.cashflowVersion())
                .tradeID(ids.tradeID())
                .tradeVersion(ids.tradeVersion())
                // Values that differ for counter side of the FX deal
                .counterpartyCode(counterpartyCode)
                .entityCode(entityCode)
                .currCode(currCode)
                .payOrReceive(fxSide1Msg.payOrReceive() == PayOrReceive.RECEIVE ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                // The amount of the other side of FX trade is also NOT calculated based on rate. It is just a random number
                .amount(BigDecimal.valueOf(getFxSide2Amount(fxSide1Msg.amount().doubleValue())));
        // bookCode and counterBookCode are not changed as they are dummy values as of now
    }

    @Override
    protected void buildCashMessageAmendmentContext(Consumer<CashMessageAmendmentContext> buildAmendedMessageFunc, FxTradeContext trdCtxForAmendment) {
        var rootMsg = trdCtxForAmendment.rootFoCashMessage();
        var nextTradeEventAction = determineNextTradeEventAndAction(rootMsg.tradeEventType(), rootMsg.tradeEventAction());
        var nextEventType = nextTradeEventAction.event();
        var msgAmndCtx = switch (nextEventType) {
            // Common trade amend events applicable for all trades
            case AMEND, REBOOK -> buildAmendmentContextForCommonAmendEvents(trdCtxForAmendment, nextTradeEventAction);
            case CANCEL -> buildAmendmentContextForCancelEvent(nextTradeEventAction);
            case BOOK_MOVE -> throw new RuntimeException("Trade amendment for BOOK_MOVE event is not implemented yet");
            // Trade specific amendments are not permitted
            case TERMINATE, ROLL, EXERCISE, CORRECTION, KNOCK_OUT, EXPIRE, FIX, UN_FIX, INTEREST_ACTION, MATURE ->
                    throw new RuntimeException("Invalid trade event and action. Amendment for trade specific event: " + nextEventType + " is not permitted");
            case NEW_TRADE -> throw new RuntimeException("Invalid trade event and action. NEW_TRADE is not amendment");
        };

        // Execute the amendment build function
        buildAmendedMessageFunc.accept(msgAmndCtx);
    }

    private CashMessageAmendmentContext buildAmendmentContextForCancelEvent(TradeEventActionPair nextTradeEventAction) {
        // No field need to be amended because this is trade cancellation
        return new CashMessageAmendmentContext(nextTradeEventAction);
    }

    private CashMessageAmendmentContext buildAmendmentContextForCommonAmendEvents(FxTradeContext trdCtxForAmendment, TradeEventActionPair nextTradeEventAction) {
        final var side1Msg = trdCtxForAmendment.side1Msg();
        final var side2Msg = trdCtxForAmendment.side2Msg();
        Set<AmendableFoCashMessageFieldType> amendableFieldTypes = cyclicAmendableFoCashMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        for (AmendableFoCashMessageFieldType ft : amendableFieldTypes) {
            switch (ft) {
                case VALUE_DATE -> {
                    var valueDate = new AmendableFoCashMessageField.ValueDate(msgTemplateHelper.getRndmValueDate());
                    amendableFields
                            .add(FX_SIDE1, valueDate)
                            .add(FX_SIDE2, valueDate);
                }
                case AMOUNT -> {
                    var side1AmntVal = rndm.nextDouble(2, 95036);
                    var side1Amount = new AmendableFoCashMessageField.Amount(BigDecimal.valueOf(side1AmntVal));
                    var side2Amount = new AmendableFoCashMessageField.Amount(BigDecimal.valueOf(getFxSide2Amount(side1AmntVal)));

                    amendableFields
                            .add(FX_SIDE1, side1Amount)
                            .add(FX_SIDE2, side2Amount);
                }
                case COUNTERPARTY_CODE -> {
                    String side1CpCodeStr = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(side1Msg.counterpartyCode());
                    String side2CpCodeStr = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(side2Msg.counterpartyCode());
                    var side1CpCode = new AmendableFoCashMessageField.CounterpartyCode(side1CpCodeStr);
                    var side2CpCode = new AmendableFoCashMessageField.CounterpartyCode(side2CpCodeStr);

                    // NOTE: It is possible for one of the side of the fx trade to get new cpCode same as the old cpCode of the counter side. This is perfectly fine and is still a correct behaviour
                    amendableFields
                            .add(FX_SIDE1, side1CpCode)
                            .add(FX_SIDE2, side2CpCode);
                }
            }
        }

        var side1AmndSubCtx = new AmendmentSubjectContextEager(FX_SIDE1, side1Msg, trdCtxForAmendment::setSide1Msg, amendableFields.get(FX_SIDE1));
        var side2AmndSubCtx = new AmendmentSubjectContextEager(FX_SIDE2, side2Msg, trdCtxForAmendment::setSide2Msg, amendableFields.get(FX_SIDE2));
        return new CashMessageAmendmentContext(nextTradeEventAction)
                .addNextAmndSubCtx(side1AmndSubCtx)
                .addNextAmndSubCtx(side2AmndSubCtx);
    }

    private double getFxSide2Amount(double side1Amount) {
        return rndm.nextDouble(Math.abs(side1Amount - 100), side1Amount + 1000);
    }

    private static List<Set<AmendableFoCashMessageFieldType>> getListOfAmendableCashMessageFieldTypes() {
        return List.of(
                Set.of(COUNTERPARTY_CODE),
                Set.of(AMOUNT),
                Set.of(VALUE_DATE, AMOUNT),
                Set.of(COUNTERPARTY_CODE, AMOUNT),
                Set.of(VALUE_DATE, AMOUNT, COUNTERPARTY_CODE)
        );
    }

    @Override
    protected TradeEventActionPair determineNextTradeEventAndAction(TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        return msgTemplateHelper.determineNextTradeEventAndActionForCommonEvents(rndm, tradeEventType, tradeEventAction);
    }

    @Override
    protected CashMessageStoreHelper<FxTradeContext> msgStoreHelper() {
        return msgStoreHelper;
    }

    private static class CyclicAmendableFoCashMessageFieldProvider extends AbstractCyclicDataProvider<Set<AmendableFoCashMessageFieldType>> {
        public CyclicAmendableFoCashMessageFieldProvider(List<Set<AmendableFoCashMessageFieldType>> fields) {
            super(fields);
        }
    }
}