package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.domain.InterestBasis;
import io.alw.css.fosimulator.template.domain.InterestPayoutFrequency;
import io.alw.css.fosimulator.template.domain.InterestTradeLeg;
import io.alw.css.fosimulator.template.domain.MmTradeContext;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.common.PayOrReceive.PAY;
import static io.alw.css.domain.common.PayOrReceive.RECEIVE;
import static io.alw.css.domain.common.RateType.FLOAT;
import static io.alw.css.domain.common.TradeType.MM_CALL;
import static io.alw.css.domain.common.TradeType.MM_TERM;
import static io.alw.css.domain.trade.TradeLegType.*;
import static io.alw.css.fosimulator.template.MmTemplateConstants.*;

public final class MmTemplate extends CashMessageAmendmentTemplate<MmTradeContext> {
    private final CashMessageStoreHelper<MmTradeContext> msgStoreHelper;

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);

        CashMessageStore<MmTradeContext> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    /// Build new template for MM cashflow. A new MM trade can have 1 to 3 cashMessages depending on whether its a TERM or CALL and depending on the interest cashflow
    @Override
    public MmTemplate withRootTemplateValues() {
        // Create MessageContext
        MmTradeContext trdCtx = createMmTrade();
        // Create MoneyMarket trade builder with base values
        TradeBuilder bdr = getBaseCashMsgBuilder(trdCtx);
        // Build PRINCIPAL leg
        var newTrdEventAndAction = new TradeEventActionPair(TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        var principalLegId = new Id(trdCtx.nextTradeLegId(), VERSION_ONE);
        this.withGroupedItem(trdCtx::setRootTradeLeg, () -> buildPrincipalLeg(principalLegId, newTrdEventAndAction));

        // IMPORTANT NOTE: The order here is important.
        // MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
        // The lambdas added via [io.alw.datagen.template.AggregateTemplateBuilder#withGroupedItem(Supplier)] method will be executed strictly in the same order as they are inserted in the queue
        switch (trdCtx.trade().tradeType()) {
            case MM_TERM -> {
                var interestLegIds = CashMessageTemplateHelper.getNewTradeLegId(trdCtx);
                var maturityLegIds = CashMessageTemplateHelper.getNewTradeLegId(trdCtx);
                buildMaturityAndInterestLeg(trdCtx, maturityLegIds, interestLegIds, newTrdEventAndAction);
            }
            case MM_CALL -> {
                var interestLegIds = CashMessageTemplateHelper.getNewTradeLegId(trdCtx);
                this.withGroupedItem(trdCtx::addInterestLeg, () -> buildInterestLeg(trdCtx, interestLegIds, newTrdEventAndAction));
            }
            default -> throw new RuntimeException("Invalid TradeType for MmTemplate");
        }
        return this;
    }

    private TradeLegBuilder buildPrincipalLeg(Id id, TradeEventActionPair trdEventAndAction) {
        return TradeLegBuilder.builder()
                .tradeLegId(id.Id())
                .tradeLegVersion(id.version())
                .tradeLegType(MM_PRINCIPAL)
                .rate(cyclicRateProvider.get()) // rate is just a constant from a list of multiple rate constants. No rate dependent calculation is done in CSS
                .currCode(this.currCode)
                .tradeEventType(trdEventAndAction.event())
                .tradeEventAction(trdEventAndAction.action())
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound)))
                ;
    }

    @Override
    protected void buildCashMessageAmendmentContext(Consumer<TradeAmendmentContext> amendmentMessageBuilderFunc, MmTradeContext trdForAmendment) {
        switch (cyclicAmendableMmLegProvider.get()) {
            case MM_PRINCIPAL -> {
                var msgAmendCtx = buildCashMessageAmendmentContextStep2(trdForAmendment.principalLeg(), trdForAmendment);
                amendmentMessageBuilderFunc.accept(msgAmendCtx);
            }
            case MM_MATURITY -> {
                var maturityLeg = trdForAmendment.maturityLeg();
                // maturityLeg may not be present for CALL trade. If not present, do no amendment to create, hence do not execute the function
                if (maturityLeg != null) {
                    var msgAmendCtx = buildCashMessageAmendmentContextStep2(maturityLeg, trdForAmendment);
                    amendmentMessageBuilderFunc.accept(msgAmendCtx);
                }
            }
            case MM_INTEREST -> throw new RuntimeException("Amending interest leg is not allowed");
            default -> throw new RuntimeException("Invalid cash leg type for MM Trade");
        }
    }

    private TradeAmendmentContext buildCashMessageAmendmentContextStep2(TradeLeg primaryAmendmentSubject, MmTradeContext trd) {
        var nextTradeEventAction = determineNextTradeEventAndAction(trd.trade().tradeEventType(), trd.trade().tradeEventAction());
        var nextEventType = nextTradeEventAction.event();
        return switch (nextEventType) {
            // Common trade amend events applicable for all trades
            case AMEND, REBOOK -> buildAmendmentContextForCommonAmendEvents(primaryAmendmentSubject.tradeLegType(), trd, nextTradeEventAction);
            case CANCEL -> buildAmendmentContextForCancelEvent(nextTradeEventAction);
            case BOOK_MOVE -> throw new RuntimeException("Trade amendment for BOOK_MOVE event is not implemented yet");
            // Trade specific amendments are not permitted
            case TERMINATE, ROLL, EXERCISE, CORRECTION, KNOCK_OUT, EXPIRE, FIX, UN_FIX, INTEREST_ACTION, COUPON, MATURE ->
                    throw new RuntimeException("Invalid trade event and action. Amendment for trade specific event: " + nextEventType + " is not permitted");
            case NEW_TRADE -> throw new RuntimeException("Invalid trade event and action. NEW_TRADE is not amendment");
        };
    }

    /// NOTE: This method is used to handle only cancel event, [TradeEventType#CANCEL], for both [TradeLegType#MM_PRINCIPAL] and [TradeLegType#MM_MATURITY]
    private TradeAmendmentContext buildAmendmentContextForCancelEvent(TradeEventActionPair nextTradeEventAction) {
        // No field need to be amended because this is trade cancellation
        return new TradeAmendmentContext(nextTradeEventAction);
    }

    /// NOTE: This method is used to handle common amendment events, [TradeEventType#AMEND] and [TradeEventType#REBOOK], for both [TradeLegType#MM_PRINCIPAL] and [TradeLegType#MM_MATURITY]
    private TradeAmendmentContext buildAmendmentContextForCommonAmendEvents(TradeLegType primaryAmendmentSubjectTradeLegType, MmTradeContext trd, TradeEventActionPair nextTradeEventAction) {
        var principalLeg = trd.principalLeg();
        var maturityLeg = trd.maturityLeg(); // NOTE: MaturityLeg could be null for MM CALL trades

        Set<AmendableFoCashMessageFieldType> amendableFieldTypes = cyclicAmendableFoCashMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        // NOTE: To determine new amount for InterestLeg, the amended amount and amended valueDate of both *principalLeg* and *maturityLeg* are needed, although they may not be computed yet during execution
        // One of the `AmendableFoCashMessageFieldSupplier` type is used to lazily obtain the amendable fields after the amended principalLeg and maturityLeg are built.
        var intLegAmndFieldSupplier = new AmendableFieldSupplier.SupplierWithMessageSelector(trd, givenTrd -> ((MmTradeContext) givenTrd).interestLegs().stream().filter(tl -> tl.valueDate().isAfter(msgTemplateHelper.currentDateForMsgTemplate())).toList());
        for (var ft : amendableFieldTypes) {
            switch (ft.amendmentTarget()) {
                case TRADE -> {
                    switch (ft) {
                        case COUNTERPARTY_CODE -> {
                            var cpCode = MmAmendmentFieldValue.PrimarySubject.forCounterpartyCode(trd, msgTemplateHelper);
                            amendableFields.addForTrade(cpCode);
                        }
                        case VALUE_DATE, AMOUNT -> throw new RuntimeException("ValueDate and Amount amendments is not possible on Trade level. Instead, it must be done on TradeLeg level");
                    }
                }
                case TRADE_LEG -> {
                    switch (ft) {
                        case COUNTERPARTY_CODE -> throw new RuntimeException("CounterpartyCode amendment is not possible on TradeLeg level. Instead, it must be done on Trade level");
                        case AMOUNT -> {
                            var amount = MmAmendmentFieldValue.PrimarySubject.forAmount(rndm);
                            intLegAmndFieldSupplier.add(tl -> new AmendableField.Amount(determineInterestLegAmount(trd, principalLeg, maturityLeg, (InterestTradeLeg) tl)));
                            amendableFields
                                    .addForTradeLeg(MM_PRINCIPAL, amount)
                                    .addForTradeLeg(MM_INTEREST, intLegAmndFieldSupplier);
                            if (maturityLeg != null) {
                                amendableFields.addForTradeLeg(MM_MATURITY, amount); // The Pay/Receive direction remains the same
                            }
                        }
                        case VALUE_DATE -> {
                            if (primaryAmendmentSubjectTradeLegType == MM_PRINCIPAL) {
                                // No adjustments needed for MM_INTEREST with respect to valueDate change of principalLeg

                                // Get new valueDate for principalLeg and add to the collection
                                var principalLegNewVd = MmAmendmentFieldValue.PrimarySubject.PrincipalLeg.forValueDate(principalLeg, msgTemplateHelper, trd);
                                amendableFields.addForTradeLeg(MM_PRINCIPAL, principalLegNewVd);

                                // Conditionally apply new valueDate for maturityLeg
                                if (maturityLeg != null) {
                                    // Condition
                                    Predicate<TradeLeg> matLegVdAmendmentCondition = ml ->
                                            ml != null
                                                    && principalLeg.valueDate().until(ml.valueDate(), ChronoUnit.DAYS) < 10;

                                    // Create conditional valueDate supplier for maturityLeg and add to the collection
                                    var matLegVdConditionalSupplier = new AmendableFieldSupplier
                                            .ConditionalSupplier(maturityLeg, matLegVdAmendmentCondition)
                                            .add(givenMatCashLeg -> {
                                                // Determine new valueDate for maturityLeg
                                                var principalLegOldVd = principalLeg.valueDate();
                                                long daysDiff = principalLegOldVd.until(principalLegNewVd.date(), ChronoUnit.DAYS);
                                                LocalDate newMatValueDate = givenMatCashLeg.valueDate().plusDays(daysDiff);
                                                return new AmendableField.ValueDate(newMatValueDate);
                                            });

                                    amendableFields.addForTradeLeg(MM_MATURITY, matLegVdConditionalSupplier);
                                }
                            } else if (primaryAmendmentSubjectTradeLegType == MM_MATURITY && maturityLeg != null) {
                                // Determine new valueDate for maturityLeg and add to the collection
                                LocalDate newValueDate = determineMaturityLegValueDate(maturityLeg);
                                amendableFields.addForTradeLeg(MM_MATURITY, new AmendableField.ValueDate(newValueDate));
                                // NOTE: No adjustments to any other cash legs are required when valueDate of maturityLeg is amended
                            }
                        }
                    }
                }
            }
        }

        // Create TradeAmendmentContext
        var tradeLevelAmendableFields = amendableFields.getForTrade();
        var amndCtx = new TradeAmendmentContext(nextTradeEventAction, tradeLevelAmendableFields);

        // Amendment context for PrincipalLeg
        var fieldsForPrincipalLeg = amendableFields.getForTradeLeg(MM_PRINCIPAL);
        if (fieldsForPrincipalLeg != null) {
            var principalAmndCtx = new TradeLegAmendmentContextEager(principalLeg, trd::setRootTradeLeg, fieldsForPrincipalLeg);
            amndCtx.addNextTradeLegAmndCtx(principalAmndCtx);
        } else if (nextTradeEventAction.event() == TradeEventType.REBOOK) {
            // In case of a REBOOK event, the PRINCIPAL and MATURITY leg must be rebooked. So add a dummy amendment to include the PRINCIPAL leg
            var valueDate = new AmendableField.ValueDate(principalLeg.valueDate());
            var fieldSet = new HashSet<AmendableField>();
            fieldSet.add(valueDate);
            var principalAmndCtx = new TradeLegAmendmentContextEager(principalLeg, trd::setRootTradeLeg, fieldSet);
            amndCtx.addNextTradeLegAmndCtx(principalAmndCtx);
        }
        // Amendment context for MaturityLeg(could be null for CALL trades)
        var fieldsForMaturityLeg = amendableFields.getForTradeLeg(MM_MATURITY);
        if (fieldsForMaturityLeg != null) {
            var maturityAmndCtx = new TradeLegAmendmentContextLazy(maturityLeg, _ -> trd::setMaturityLeg, fieldsForMaturityLeg);
            amndCtx.addNextTradeLegAmndCtx(maturityAmndCtx);
        } else if (maturityLeg != null && nextTradeEventAction.event() == TradeEventType.REBOOK) {
            // In case of a REBOOK event, the PRINCIPAL and MATURITY leg must be rebooked. So add a dummy amendment to include the MATURITY leg
            var valueDate = new AmendableField.ValueDate(maturityLeg.valueDate());
            var fieldSet = new HashSet<AmendableField>();
            fieldSet.add(valueDate);
            var maturityAmndCtx = new TradeLegAmendmentContextEager(maturityLeg, trd::setMaturityLeg, fieldSet);
            amndCtx.addNextTradeLegAmndCtx(maturityAmndCtx);
        }
        // Amendment context for InterestLegs
        var fieldsForInterestLegs = amendableFields.getForTradeLeg(MM_INTEREST);
        var interestAmndCtx = new TradeLegAmendmentContextLazy(_ -> trd::addInterestLeg, fieldsForInterestLegs);
        amndCtx.addNextTradeLegAmndCtx(interestAmndCtx);

        return amndCtx;
    }

    /// IMPORTANT NOTE: The order here is important.
    /// MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
    /// The lambdas added via [AggregateTemplateBuilder#withGroupedItem(Consumer, Supplier)] method will be executed strictly in the same order as they are inserted in the queue
    private void buildMaturityAndInterestLeg(MmTradeContext trd, Id maturityLegId, Id interestLegId, TradeEventActionPair trdEventAndAction) {
        // 1. Add building function of MATURITY leg
        this.withGroupedItem(trd::setMaturityLeg, () -> buildMaturityLeg(trd, maturityLegId, trdEventAndAction));
        // 2. Add building function of INTEREST leg
        this.withGroupedItem(trd::addInterestLeg, () -> buildInterestLeg(trd, interestLegId, trdEventAndAction));
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    /// This method is intended to be used not only for the first interest leg but also for future interest legs. Therefor Ids are received as a parameter. The same pattern is followed to build maturity leg although there can be only one maturityLeg
    private TradeLegBuilder buildInterestLeg(MmTradeContext trd, Id interestLegId, TradeEventActionPair trdEventAndAction) {
        var principalLeg = trd.principalLeg();
        var maturityLeg = trd.maturityLeg(); // NOTE: the 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
        InterestTradeLeg interestLeg = trd.interestLegs().getFirst();

        // Build the INTEREST leg
        var bdr = createBuilderFrom(principalLeg)
                .tradeLegId(interestLegId.Id())
                .tradeLegVersion(interestLegId.version())
                .tradeLegType(MM_INTEREST)
                .tradeEventType(trdEventAndAction.event())
                .tradeEventAction(trdEventAndAction.action())
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(trd, principalLeg, maturityLeg))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(determineInterestLegAmount(trd, principalLeg, maturityLeg, interestLeg))
                // no change for currCode
                ;

        if (trd.rateType() == FLOAT && (VERSION_ONE != bdr.tradeLegVersion() || VERSION_ONE != trd.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    /// NOTE: The amount returned is not a result of a proper calculation based on rate.
    /// It is just a meaningful enough number for an interest leg when the following parameters are taken into consideration:
    /// - principalAmount, interest basis, interest payout frequency, rate type and maturity date
    ///
    /// This is done solely to avoid calculations using BigDecimal. Actual amount is not required.
    private BigDecimal determineInterestLegAmount(MmTradeContext trd, TradeLeg principalLeg, TradeLeg maturityLeg, InterestTradeLeg interestLeg) {
        final LocalDate maturityLegValueDate;
        if (maturityLeg == null) {
            maturityLegValueDate = principalLeg.valueDate().plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
        } else {
            maturityLegValueDate = maturityLeg.valueDate();
        }

        // Re-use the interest amount if it was already determined, but only if the values used to determine has not changed
        var intrLegCtx = interestLeg.interestLegContext();
        if (intrLegCtx != null) {
            var lastUsedInterestAmount = intrLegCtx.lastUsedInterestAmount();
            var lastUsedPrincipalAmount = intrLegCtx.lastUsedPrincipalAmount();
            var lastUsedPrincipalValueDate = intrLegCtx.lastUsedPrincipalValueDate();
            var lastUsedMaturityValueDate = intrLegCtx.lastUsedMaturityValueDate();

            if (lastUsedPrincipalAmount.equals(principalLeg.amount())
                    && lastUsedPrincipalValueDate.equals(principalLeg.valueDate())
                    && lastUsedMaturityValueDate.equals(maturityLegValueDate)) {
                return switch (trd.rateType()) {
                    case FIXED -> lastUsedInterestAmount;
                    case FLOAT -> {
                        BigDecimal newIntAmt = BigDecimal.valueOf(lastUsedInterestAmount.doubleValue() + 158);// Even though rate decrease the amount increases. This is ok! Actual amount is not required
                        intrLegCtx.setLastUsedInterestAmount(newIntAmt);
                        yield newIntAmt;
                    }
                };
            }
        }

        // Determine the interest amount
        var principalAmount = principalLeg.amount();
        var principalValueDate = principalLeg.valueDate();
        var newInterestAmount = switch (trd.interestBasis()) {
            case ThirtyBy360 -> {
                long numOfDays = ChronoUnit.DAYS.between(principalValueDate, maturityLegValueDate);
                double tenPercentOfPrincipal = (principalAmount.doubleValue() / 100) * 10;

                yield switch (trd.ipFrequency()) {
                    case DAY -> BigDecimal.valueOf(tenPercentOfPrincipal / (double) numOfDays);
                    case MONTHLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 30));
                    case QUARTERLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 90));
                    case SEMI_ANNUALLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 180));
                    case YEARLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 360));
                    case PRINCIPAL_MATURITY -> BigDecimal.valueOf(tenPercentOfPrincipal);
                };
            }
        };

        interestLeg.setInterestLegContext(new InterestLegContext(newInterestAmount, principalAmount, principalValueDate, maturityLegValueDate));
        return newInterestAmount;
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    private LocalDate determineInterestLegValueDate(MmTradeContext trd, TradeLeg principalLeg, TradeLeg maturityLeg) {
        final LocalDate principalLegValueDate = principalLeg.valueDate();
        final LocalDate maturityLegValueDate;
        if (maturityLeg == null) {
            maturityLegValueDate = principalLegValueDate.plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
        } else {
            maturityLegValueDate = maturityLeg.valueDate();
        }

        return switch (trd.interestBasis()) {
            case ThirtyBy360 -> switch (trd.ipFrequency()) {
                case DAY -> msgTemplateHelper.getFutureValueDate(1, principalLegValueDate, maturityLegValueDate);
                case MONTHLY -> msgTemplateHelper.getFutureValueDate(30, principalLegValueDate, maturityLegValueDate);
                case QUARTERLY -> msgTemplateHelper.getFutureValueDate(90, principalLegValueDate, maturityLegValueDate);
                case SEMI_ANNUALLY -> msgTemplateHelper.getFutureValueDate(180, principalLegValueDate, maturityLegValueDate);
                case YEARLY -> msgTemplateHelper.getFutureValueDate(360, principalLegValueDate, maturityLegValueDate);
                case PRINCIPAL_MATURITY -> maturityLegValueDate;
            };
        };
    }

    private TradeLegBuilder buildMaturityLeg(MmTradeContext trd, Id maturityLegId, TradeEventActionPair trdEventAndAction) {
        var principalLeg = trd.principalLeg();
        var maturityLegValueDate = determineMaturityLegValueDate(principalLeg);

        // Build the MATURITY leg
        var bdr = createBuilderFrom(principalLeg)
                .tradeLegId(maturityLegId.Id())
                .tradeLegVersion(maturityLegId.version())
                .tradeLegType(MM_MATURITY)
                .tradeEventType(trdEventAndAction.event())
                .tradeEventAction(trdEventAndAction.action())
                // Values that differ from PRINCIPAL leg
                .valueDate(maturityLegValueDate)
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(principalLeg.amount().negate())
                // No change to currCode
                ;

        if (trd.rateType() == FLOAT && (VERSION_ONE != bdr.tradeLegVersion() || VERSION_ONE != trd.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    private LocalDate determineMaturityLegValueDate(TradeLeg relativeTradeLeg) {
        return msgTemplateHelper.getRndmFutureValueDateRelativeTo(relativeTradeLeg.valueDate(), false, 10);
    }

    /// Interest Basis is always assigned a constant: InterestBasis.ThirtyBy360. Corresponding calculation for other basis types are not implemented.
    /// Interest cashflow should be generated based on [InterestPayoutFrequency]
    /// Returns message contexts for cashflow version 1. The method parameter `principalLegBuilder` must be of cashflow version 1
    private MmTradeContext createMmTrade() {
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = InterestBasis.ThirtyBy360;

        return switch (this.tradeType) {
            case MM_TERM -> new MmTradeContext(MM_TERM, rateType, ipFrequency, basis);
            case MM_CALL -> new MmTradeContext(MM_CALL, rateType, ipFrequency, basis);
            default -> throw new IllegalStateException("Invalid TradeType: " + this.tradeType + " for an MmTemplate");
        };
    }

    @Override
    protected Predicate<MmTradeContext> tradeContextAmendmentFrequency() {
        return _ -> rndm.nextInt(0, 100) > 80;
    }

    @Override
    protected TradeEventActionPair determineNextTradeEventAndAction(TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        return msgTemplateHelper.determineNextTradeEventAndActionForCommonEvents(rndm, tradeEventType, tradeEventAction);
    }

    @Override
    protected CashMessageStoreHelper<MmTradeContext> msgStoreHelper() {
        return msgStoreHelper;
    }

    private static final class MmAmendmentFieldValue {
        private static final class PrimarySubject {
            // Common methods for every PrimarySubject
            private static AmendableField forAmount(RandomGenerator rndm) {
                var newAmount = BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound));
                return new AmendableField.Amount(newAmount);
            }

            private static AmendableField forCounterpartyCode(Trade trd, CashMessageTemplateHelper msgTemplateHelper) {
                String newCounterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(trd.counterpartyCode());
                return new AmendableField.CounterpartyCode(newCounterpartyCode);
            }

            private static final class PrincipalLeg {
                private static AmendableField.ValueDate forValueDate(TradeLeg principalLeg, CashMessageTemplateHelper msgTemplateHelper, MmTradeContext trdCtx) {

                    var currentDate = msgTemplateHelper.currentDateForMsgTemplate();
                    var maturityLeg = trdCtx.maturityLeg();
                    final LocalDate maturityLegValueDate;
                    if (maturityLeg == null) {
                        maturityLegValueDate = principalLeg.valueDate().plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
                    } else {
                        maturityLegValueDate = maturityLeg.valueDate();
                    }
                    // New valueDate for principal leg
                    LocalDate newValueDate = msgTemplateHelper.getFutureValueDate(1, currentDate, maturityLegValueDate);
                    return new AmendableField.ValueDate(newValueDate);
                }
            }
        }
    }
}
