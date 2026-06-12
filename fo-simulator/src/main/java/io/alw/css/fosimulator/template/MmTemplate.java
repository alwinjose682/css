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
import io.alw.css.fosimulator.template.domain.MmTrade;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.common.PayOrReceive.PAY;
import static io.alw.css.domain.common.PayOrReceive.RECEIVE;
import static io.alw.css.domain.common.RateType.FLOAT;
import static io.alw.css.domain.trade.TradeLegType.*;
import static io.alw.css.fosimulator.template.MmTemplateConstants.*;

public final class MmTemplate extends CashMessageAmendmentTemplate<MmTrade> {
    private final CashMessageStoreHelper<MmTrade> msgStoreHelper;

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);

        CashMessageStore<MmTrade> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    /// Build new template for MM cashflow. A new MM trade can have 1 to 3 cashMessages depending on whether its a TERM or CALL and depending on the interest cashflow
    @Override
    public MmTemplate withRootTemplateValues() {
        // Create MessageContext
        MmTrade trd = createMmTrade();
        // Create Ids for PRINCIPAL, INTEREST and if applicable for MATURITY as well
        Map<TradeLegType, Ids> idsMap = createFirstVersionIds(trd);
        // Build PRINCIPAL leg of the MoneyMarket trade with base values
        MutableTradeBuilder bdr = getBaseCashMsgBuilder(idsMap.get(MM_PRINCIPAL), trd, MM_PRINCIPAL);
        // Set values specific to the PRINCIPAL leg
        bdr.tradeLegs(MM_PRINCIPAL)
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound)))
        ;

        // IMPORTANT NOTE: The order here is important.
        // MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
        // The lambdas added via [io.alw.datagen.template.AggregateTemplateBuilder#withGroupedItem(Supplier)] method will be executed strictly in the same order as they are inserted in the queue
        switch (trd.tradeType()) {
            case MM_TERM -> buildMaturityAndInterestLeg(trd, idsMap);
            case MM_CALL -> this.withGroupedItem(trd::addInterestLeg, () -> buildInterestLeg(trd, idsMap.get(MM_INTEREST)));
            default -> throw new RuntimeException("Invalid TradeType for MmTemplate");
        }
        return this;
    }

    @Override
    protected void buildCashMessageAmendmentContext(Consumer<CashMessageAmendmentContext> amendmentMessageBuilderFunc, MmTrade trdForAmendment) {
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

    private CashMessageAmendmentContext buildCashMessageAmendmentContextStep2(TradeLeg primaryAmendmentSubject, MmTrade trdForAmendment) {
        var nextTradeEventAction = determineNextTradeEventAndAction(primaryAmendmentSubject.tradeEventType(), primaryAmendmentSubject.tradeEventAction());
        var nextEventType = nextTradeEventAction.event();
        return switch (nextEventType) {
            // Common trade amend events applicable for all trades
            case AMEND, REBOOK -> buildAmendmentContextForCommonAmendEvents(primaryAmendmentSubject.tradeLegType(), trdForAmendment, nextTradeEventAction);
            case CANCEL -> buildAmendmentContextForCancelEvent(nextTradeEventAction);
            case BOOK_MOVE -> throw new RuntimeException("Trade amendment for BOOK_MOVE event is not implemented yet");
            // Trade specific amendments are not permitted
            case TERMINATE, ROLL, EXERCISE, CORRECTION, KNOCK_OUT, EXPIRE, FIX, UN_FIX, INTEREST_ACTION, COUPON, MATURE ->
                    throw new RuntimeException("Invalid trade event and action. Amendment for trade specific event: " + nextEventType + " is not permitted");
            case NEW_TRADE -> throw new RuntimeException("Invalid trade event and action. NEW_TRADE is not amendment");
        };
    }

    /// NOTE: This method is used to handle only cancel event, [TradeEventType#CANCEL], for both [TradeLegType#MM_PRINCIPAL] and [TradeLegType#MM_MATURITY]
    private CashMessageAmendmentContext buildAmendmentContextForCancelEvent(TradeEventActionPair nextTradeEventAction) {
        // No field need to be amended because this is trade cancellation
        return new CashMessageAmendmentContext(nextTradeEventAction);
    }

    /// NOTE: This method is used to handle common amendment events, [TradeEventType#AMEND] and [TradeEventType#REBOOK], for both [TradeLegType#MM_PRINCIPAL] and [TradeLegType#MM_MATURITY]
    private CashMessageAmendmentContext buildAmendmentContextForCommonAmendEvents(TradeLegType primaryAmendmentSubjectTradeLegType, MmTrade trdCtx, TradeEventActionPair nextTradeEventAction) {
        MmTradeLeg principalLeg = trdCtx.principalLeg();
        MmTradeLeg maturityLeg = trdCtx.maturityLeg(); // NOTE: MaturityLeg could be null for MM CALL trades

        Set<AmendableFoCashMessageFieldType> amendableFieldTypes = cyclicAmendableFoCashMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        // NOTE: To determine new amount for InterestLeg, the amended amount and amended valueDate of both *principalLeg* and *maturityLeg* are needed, although they may not be computed yet during execution
        // One of the `AmendableFoCashMessageFieldSupplier` type is used to lazily obtain the amendable fields after the amended principalLeg and maturityLeg are built.
        var intLegAmndFieldSupplier = new AmendableFoCashMessageFieldSupplier.SupplierWithMessageSelector(trdCtx, givenTrdCtx -> ((MmTrade) givenTrdCtx).interestLegs().stream().filter(cl -> cl.tradeLeg().valueDate().isAfter(msgTemplateHelper.currentDateForMsgTemplate())).toList());
        for (var ft : amendableFieldTypes) {
            switch (ft) {
                case AMOUNT -> {
                    var amount = MmAmendmentFieldValue.PrimarySubject.forAmount(rndm);
                    intLegAmndFieldSupplier.add(cl -> new AmendableFoCashMessageField.Amount(determineInterestLegAmount(principalLeg, maturityLeg, (InterestTradeLeg) cl)));
                    amendableFields
                            .add(MM_PRINCIPAL, amount)
                            .add(MM_INTEREST, intLegAmndFieldSupplier);
                    if (maturityLeg != null) {
                        amendableFields.add(MM_MATURITY, amount); // The Pay/Receive direction remains the same
                    }
                }
                case COUNTERPARTY_CODE -> {
                    var cpCode = MmAmendmentFieldValue.PrimarySubject.forCounterpartyCode(principalLeg, msgTemplateHelper);
                    amendableFields
                            .add(MM_PRINCIPAL, cpCode)
                            .add(MM_INTEREST, intLegAmndFieldSupplier.add(_ -> cpCode));
                    if (maturityLeg != null) {
                        amendableFields.add(MM_MATURITY, cpCode);
                    }
                }
                case VALUE_DATE -> {
                    if (primaryAmendmentSubjectTradeLegType == MM_PRINCIPAL) {
                        // No adjustments needed for MM_INTEREST with respect to valueDate change of principalLeg

                        // Get new valueDate for principalLeg and add to the collection
                        var principalLegNewVd = MmAmendmentFieldValue.PrimarySubject.PrincipalLeg.forValueDate(principalLeg, msgTemplateHelper, trdCtx);
                        amendableFields.add(MM_PRINCIPAL, principalLegNewVd);

                        // Conditionally apply new valueDate for maturityLeg
                        if (maturityLeg != null) {
                            // Condition
                            Predicate<ExtendedTradeLeg> matLegVdAmendmentCondition = ml ->
                                    ml != null && ml.tradeLeg() != null
                                            && principalLeg.tradeLeg().valueDate().until(ml.tradeLeg().valueDate(), ChronoUnit.DAYS) < 10;

                            // Create conditional valueDate supplier for maturityLeg and add to the collection
                            var matLegVdConditionalSupplier = new AmendableFoCashMessageFieldSupplier
                                    .ConditionalSupplier(maturityLeg, matLegVdAmendmentCondition)
                                    .add(givenMatCashLeg -> {
                                        // Determine new valueDate for maturityLeg
                                        var principalLegOldVd = principalLeg.tradeLeg().valueDate();
                                        long daysDiff = principalLegOldVd.until(principalLegNewVd.date(), ChronoUnit.DAYS);
                                        LocalDate newMatValueDate = givenMatCashLeg.tradeLeg().valueDate().plusDays(daysDiff);
                                        return new AmendableFoCashMessageField.ValueDate(newMatValueDate);
                                    });

                            amendableFields.add(MM_MATURITY, matLegVdConditionalSupplier);
                        }
                    } else if (primaryAmendmentSubjectTradeLegType == MM_MATURITY && maturityLeg != null) {
                        // Determine new valueDate for maturityLeg and add to the collection
                        LocalDate newValueDate = determineMaturityLegValueDate(maturityLeg);
                        amendableFields.add(MM_MATURITY, new AmendableFoCashMessageField.ValueDate(newValueDate));
                        // NOTE: No adjustments to any other cash legs are required when valueDate of maturityLeg is amended
                    }
                }
            }
        }

        // 1. Amendment context for PrincipalLeg
        var primaryAmndSubCtx = new AmendmentSubjectContextEager(principalLeg, principalLeg::setTradeLeg, amendableFields.get(MM_PRINCIPAL));
        // 2. Amendment context for MaturityLeg(could be null for CALL trades)
        AmendmentSubjectContextLazy maturityAmndSubCtx = null;
        if (maturityLeg != null) {
            maturityAmndSubCtx = new AmendmentSubjectContextLazy(maturityLeg, cl -> cl::setTradeLeg, amendableFields.get(MM_MATURITY));
        }
        // 3. Amendment context for InterestLegs
        var interestAmndCtx = new AmendmentSubjectContextLazy(cl -> cl::setTradeLeg, amendableFields.get(MM_INTEREST));

        return new CashMessageAmendmentContext(nextTradeEventAction)
                .addNextAmndSubCtx(primaryAmndSubCtx)
                .addNextAmndSubCtx(maturityAmndSubCtx)
                .addNextAmndSubCtx(interestAmndCtx);
    }

    /// IMPORTANT NOTE: The order here is important.
    /// MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
    /// The lambdas added via [AggregateTemplateBuilder#withGroupedItem] method will be executed strictly in the same order as they are inserted in the queue
    private void buildMaturityAndInterestLeg(MmTrade trd, Map<TradeLegType, Ids> idsMap) {
        Ids maturityLegIds = idsMap.get(MM_MATURITY);
        Ids interestLegIds = idsMap.get(MM_INTEREST);
        // 1. Add building function of MATURITY leg
        this.withGroupedItem(trd::setMaturityLeg, () -> buildMaturityLeg(trd, maturityLegIds));
        // 2. Add building function of INTEREST leg
        this.withGroupedItem(trd::addInterestLeg, () -> buildInterestLeg(trd, interestLegIds));
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    /// This method is intended to be used not only for the first interest leg but also for future interest legs. Therefor Ids are received as a parameter. The same pattern is followed to build maturity leg although there can be only one maturityLeg
    private TradeLegBuilder buildInterestLeg(MmTrade trd, Ids ids) {
        var principalLeg = trd.principalLeg();
        var maturityLeg = trd.maturityLeg(); // NOTE: the 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
        InterestTradeLeg interestLeg = trd.interestLegs().getFirst();

        // Build the INTEREST leg
        var bdr = createBuilderFrom(principalLeg)
                // Id and version of MATURITY leg was already determined when the PRINCIPAL was created
                .tradeLegId(ids.tradeLegId())
                .tradeLegVersion(ids.tradeLegVersion())
                .tradeLegType(MM_INTEREST)
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(trd, principalLeg, maturityLeg))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(determineInterestLegAmount(trd, principalLeg, maturityLeg, interestLeg));

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
    private BigDecimal determineInterestLegAmount(MmTrade trd, TradeLeg principalLeg, TradeLeg maturityLeg, InterestTradeLeg interestLeg) {
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
    private LocalDate determineInterestLegValueDate(MmTrade trd, TradeLeg principalLeg, TradeLeg maturityLeg) {
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

    private TradeLegBuilder buildMaturityLeg(MmTrade trd, Ids maturityLegIds) {
        var principalLeg = trd.principalLeg();
        var maturityLegValueDate = determineMaturityLegValueDate(principalLeg);

        // Build the MATURITY leg
        var bdr = createBuilderFrom(principalLeg)
                // Id and version of MATURITY leg was already determined when the PRINCIPAL was created
                .tradeLegId(maturityLegIds.tradeLegId())
                .tradeLegVersion(maturityLegIds.tradeLegVersion())
                .tradeLegType(MM_MATURITY)
                // Values that differ from PRINCIPAL leg
                .valueDate(maturityLegValueDate)
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(principalLeg.amount().negate());

        if (trd.rateType() == FLOAT && (VERSION_ONE != bdr.tradeLegVersion() || VERSION_ONE != trd.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    private LocalDate determineMaturityLegValueDate(TradeLeg relativeTradeLeg) {
        return msgTemplateHelper.getRndmFutureValueDateRelativeTo(relativeTradeLeg.valueDate(), false, 10);
    }

    private Map<TradeLegType, Ids> createFirstVersionIds(MmTrade trd) {
        var principalLegIds = CashMessageTemplateHelper.getNewTradeLegIds(trd);
        var interestLegIds = CashMessageTemplateHelper.getNewCashMsgIdsFromExistingTrade(MM_INTEREST, principalLegIds);

        return switch (trd.tradeType()) {
            case MM_TERM -> {
                var maturityLegIds = CashMessageTemplateHelper.getNewCashMsgIdsFromExistingTrade(MM_MATURITY, principalLegIds);
                yield Map.of(MM_PRINCIPAL, principalLegIds, MM_INTEREST, interestLegIds, MM_MATURITY, maturityLegIds);
            }
            case MM_CALL -> Map.of(MM_PRINCIPAL, principalLegIds, MM_INTEREST, interestLegIds);
            default -> throw new RuntimeException("Invalid TradeType for MmTemplate");
        };
    }

    /// Interest Basis is always assigned a constant: InterestBasis.ThirtyBy360. Corresponding calculation for other basis types are not implemented.
    /// Interest cashflow should be generated based on [InterestPayoutFrequency]
    /// Returns message contexts for cashflow version 1. The method parameter `principalLegBuilder` must be of cashflow version 1
    private MmTrade createMmTrade() {
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = InterestBasis.ThirtyBy360;

        return switch (this.tradeType) {
            case MM_TERM -> new MmTrade(TradeType.MM_TERM, rateType, ipFrequency, basis);
            case MM_CALL -> new MmTrade(TradeType.MM_CALL, rateType, ipFrequency, basis);
            default -> throw new IllegalStateException("Invalid TradeType: " + this.tradeType + " for an MmTemplate");
        };
    }

    @Override
    protected Predicate<MmTrade> tradeContextAmendmentFrequency() {
        return _ -> rndm.nextInt(0, 100) > 80;
    }

    @Override
    protected TradeEventActionPair determineNextTradeEventAndAction(TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        return msgTemplateHelper.determineNextTradeEventAndActionForCommonEvents(rndm, tradeEventType, tradeEventAction);
    }

    @Override
    protected CashMessageStoreHelper<MmTrade> msgStoreHelper() {
        return msgStoreHelper;
    }

    private static final class MmAmendmentFieldValue {
        private static final class PrimarySubject {
            // Common methods for every PrimarySubject
            private static AmendableFoCashMessageField forAmount(RandomGenerator rndm) {
                var newAmount = BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound));
                return new AmendableFoCashMessageField.Amount(newAmount);
            }

            private static AmendableFoCashMessageField forCounterpartyCode(Trade trd, CashMessageTemplateHelper msgTemplateHelper) {
                String newCounterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(trd.counterpartyCode());
                return new AmendableFoCashMessageField.CounterpartyCode(newCounterpartyCode);
            }

            private static final class PrincipalLeg {
                private static AmendableFoCashMessageField.ValueDate forValueDate(TradeLeg principalLeg, CashMessageTemplateHelper msgTemplateHelper, MmTrade trdCtx) {

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
                    return new AmendableFoCashMessageField.ValueDate(newValueDate);
                }
            }
        }
    }
}
