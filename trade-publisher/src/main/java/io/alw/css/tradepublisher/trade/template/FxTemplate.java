package io.alw.css.tradepublisher.trade.template;

import io.alw.css.domain.common.*;
import io.alw.css.domain.trade.TradeLegBuilder;
import io.alw.css.tradepublisher.generator.DayTicker;
import io.alw.css.tradepublisher.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.store.InMemoryStore;
import io.alw.css.tradepublisher.store.Store;
import io.alw.css.tradepublisher.store.StoreHelper;
import io.alw.css.tradepublisher.trade.model.Entity;
import io.alw.css.tradepublisher.trade.model.TradeEventActionPair;
import io.alw.css.tradepublisher.trade.service.RefDataService;
import io.alw.css.tradepublisher.trade.template.domain.FxTrade;
import io.alw.css.tradepublisher.trade.template.model.*;
import io.alw.datagen.provider.AbstractCyclicDataProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.trade.TradeLegType.FX_SIDE1;
import static io.alw.css.domain.trade.TradeLegType.FX_SIDE2;
import static io.alw.css.tradepublisher.trade.template.model.AmendableFieldType.*;

public final class FxTemplate extends TradeAmendmentTemplate<FxTrade, FxTemplate> {
    private final StoreHelper<FxTrade> trdStoreHelper;
    private final Supplier<Set<AmendableFieldType>> cyclicAmendableTradeMessageFieldTypeProvider = new CyclicAmendableTradeMessageFieldProvider(getListOfAmendableTradeMessageFieldTypes());

    public FxTemplate(Entity entity, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, TradeTemplateProperties tradeTemplateProperties) {
        super(entity, TradeType.FX, transactionType, rndm, initialValueDate, refDataService, dayTicker, tradeTemplateProperties);

        Store<FxTrade> trdStore = new InMemoryStore<>();
        this.trdStoreHelper = new StoreHelper<>(dayTicker, trdStore, rndm);
    }

    @Override
    protected Predicate<FxTrade> amendmentCandidateSelectionCriteriaSecondary() {
        return _ -> rndm.nextInt(0, 100) > 80;
    }

    @Override
    public FxTemplate withRootTemplateValues() {
        // Create message context and all tradeLinks
        var extTrd = createExtendedTrade();
        // Create Trade builder for new template with default base values
        createNewTradeWithDefaultValues(extTrd);
        // Create Ids for the FXTrade legs, FX-Side-1 and Fx-Side-2
        // Create FX-Side-1 (rootTradeLeg)
        this.withChildTemplateDirective(extTrd::setRootTradeLeg, this::buildFxSide1);
        // Create FX-Side-2
        this.withChildTemplateDirective(extTrd::setTradeLeg2, this::buildFxSide2);

        return this;
    }

    /// Builds TradeLeg-1(Fx-Side-1) of the fx message
    private TradeLegBuilder buildFxSide1() {
        return createNewTradeLegWithDefaultValues(getExtendedTradeOfCurrentBuildCycle(), FX_SIDE1)
                .valueDate(trdTemplateHelper.getRndmValueDate(50))
                .payOrReceive(rndm.nextBoolean() ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(2, 95036)));
    }

    /// Builds TradeLeg-2(Fx-Side-2) of the fx message
    private TradeLegBuilder buildFxSide2() {
        var tradeLeg1 = getExtendedTradeOfCurrentBuildCycle().tradeLeg1();
        String side2CounterpartyCode = trdTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(tradeLeg1.counterpartyCode());
        Entity side2Entity = refDataService.entityOtherThan(rndm, tradeLeg1.entityCode());
        String side2EntityCode = side2Entity.entityCode();
        String side2CurrCode = side2Entity.currCode();

        return TradeLegBuilder.builder(tradeLeg1)
                // Id and version of fxSide2
                .tradeLegId(getExtendedTradeOfCurrentBuildCycle().nextTradeLegId())
                .tradeLegVersion(VERSION_ONE)
                .tradeLegType(FX_SIDE2)
                // Values that differ for counter side of the FX deal
                .counterpartyCode(side2CounterpartyCode)
                .entityCode(side2EntityCode)
                .currCode(side2CurrCode)
                .payOrReceive(tradeLeg1.payOrReceive() == PayOrReceive.RECEIVE ? PayOrReceive.PAY : PayOrReceive.RECEIVE)
                // The amount of the other side of FX trade is also NOT calculated based on rate. It is just a random number
                .amount(BigDecimal.valueOf(getFxSide2Amount(tradeLeg1.amount().doubleValue())));
        // bookCode and counterBookCode are not changed as they are dummy values as of now
    }

    @Override
    protected void buildTradeAmendmentContext(Consumer<TradeAmendmentContext> trdAmendmentBuilderFunc, FxTrade trdCtxForAmendment) {
        var rootTrdLeg = trdCtxForAmendment.rootTradeLeg();
        var nextTradeEventAction = determineNextTradeEventAndAction(rootTrdLeg.tradeEventType(), rootTrdLeg.tradeEventAction());
        var nextEventType = nextTradeEventAction.event();
        var trdAmndCtx = switch (nextEventType) {
            // Common trade amend events applicable for all trades
            case AMEND, REBOOK -> buildAmendmentContextForCommonAmendEvents(trdCtxForAmendment, nextTradeEventAction);
            case CANCEL -> buildAmendmentContextForCancelEvent(nextTradeEventAction);
            case BOOK_MOVE -> throw new RuntimeException("Trade amendment for BOOK_MOVE event is not implemented yet");
            // Trade specific amendments are not permitted
            case TERMINATE, ROLL, EXERCISE, CORRECTION, KNOCK_OUT, EXPIRE, FIX, UN_FIX, INTEREST_ACTION, COUPON, MATURE ->
                    throw new RuntimeException("Invalid trade event and action. Amendment for trade specific event: " + nextEventType + " is not permitted");
            case NEW_TRADE -> throw new RuntimeException("Invalid trade event and action. NEW_TRADE is not amendment");
        };

        // Execute the amendment build function
        trdAmendmentBuilderFunc.accept(trdAmndCtx);
    }

    private TradeAmendmentContext buildAmendmentContextForCancelEvent(TradeEventActionPair nextTradeEventAction) {
        // No field need to be amended because this is trade cancellation
        return new TradeAmendmentContext(nextTradeEventAction);
    }

    private TradeAmendmentContext buildAmendmentContextForCommonAmendEvents(FxTrade trdCtxForAmendment, TradeEventActionPair nextTradeEventAction) {
        final var trdLeg1 = trdCtxForAmendment.tradeLeg1();
        final var trdLeg2 = trdCtxForAmendment.tradeLeg2();
        Set<AmendableFieldType> amendableFieldTypes = cyclicAmendableTradeMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        for (AmendableFieldType ft : amendableFieldTypes) {
            switch (ft) {
                case VALUE_DATE -> {
                    var valueDate = new AmendableField.ValueDate(trdTemplateHelper.getRndmValueDate());
                    amendableFields
                            .addForTradeLeg(FX_SIDE1, valueDate)
                            .addForTradeLeg(FX_SIDE2, valueDate);
                }
                case AMOUNT -> {
                    var side1AmntVal = rndm.nextDouble(2, 95036);
                    var side1Amount = new AmendableField.Amount(BigDecimal.valueOf(side1AmntVal));
                    var side2Amount = new AmendableField.Amount(BigDecimal.valueOf(getFxSide2Amount(side1AmntVal)));

                    amendableFields
                            .addForTradeLeg(FX_SIDE1, side1Amount)
                            .addForTradeLeg(FX_SIDE2, side2Amount);
                }
                case COUNTERPARTY_CODE -> {
                    String side1CpCodeStr = trdTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(trdLeg1.counterpartyCode());
                    String side2CpCodeStr = trdTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(trdLeg2.counterpartyCode());
                    var side1CpCode = new AmendableField.CounterpartyCode(side1CpCodeStr);
                    var side2CpCode = new AmendableField.CounterpartyCode(side2CpCodeStr);

                    // NOTE: It is possible for one of the side of the fx trade to get new cpCode same as the old cpCode of the counter side. This is perfectly fine and is still a correct behaviour
                    amendableFields
                            .addForTradeLeg(FX_SIDE1, side1CpCode)
                            .addForTradeLeg(FX_SIDE2, side2CpCode);
                }
            }
        }

        var side1AmndSubCtx = new TradeLegAmendmentContextEager(FX_SIDE1, trdLeg1, trdCtxForAmendment::setTradeLeg1, amendableFields.getForTradeLeg(FX_SIDE1));
        var side2AmndSubCtx = new TradeLegAmendmentContextEager(FX_SIDE2, trdLeg2, trdCtxForAmendment::setTradeLeg2, amendableFields.getForTradeLeg(FX_SIDE2));
        return new TradeAmendmentContext(nextTradeEventAction)
                .addNextTradeLegAmndCtx(side1AmndSubCtx)
                .addNextTradeLegAmndCtx(side2AmndSubCtx);
    }

    private double getFxSide2Amount(double side1Amount) {
        return rndm.nextDouble(Math.abs(side1Amount - 100), side1Amount + 1000);
    }

    private static List<Set<AmendableFieldType>> getListOfAmendableTradeMessageFieldTypes() {
        return List.of(
                Set.of(COUNTERPARTY_CODE),
                Set.of(AMOUNT),
                Set.of(VALUE_DATE, AMOUNT),
                Set.of(COUNTERPARTY_CODE, AMOUNT),
                Set.of(VALUE_DATE, AMOUNT, COUNTERPARTY_CODE)
        );
    }

    @Override
    protected FxTrade createExtendedTrade() {
        return new FxTrade();
    }

    @Override
    protected TradeEventActionPair determineNextTradeEventAndAction(TradeEventType trdEventType, TradeEventAction trdEventAction) {
        return trdTemplateHelper.determineNextTradeEventAndActionForCommonEvents(rndm, trdEventType, trdEventAction);
    }

    @Override
    protected FxTemplate self() {
        return this;
    }

    @Override
    protected StoreHelper<FxTrade> trdStoreHelper() {
        return trdStoreHelper;
    }

    private class CyclicAmendableTradeMessageFieldProvider extends AbstractCyclicDataProvider<Set<AmendableFieldType>> {
        public CyclicAmendableTradeMessageFieldProvider(List<Set<AmendableFieldType>> fields) {
            super(fields);
        }
    }
}