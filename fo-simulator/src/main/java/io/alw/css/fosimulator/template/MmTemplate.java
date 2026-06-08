package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.template.domain.*;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.cashflow.PayOrReceive.PAY;
import static io.alw.css.domain.cashflow.PayOrReceive.RECEIVE;
import static io.alw.css.domain.cashflow.RateType.FLOAT;
import static io.alw.css.fosimulator.template.MmTemplateConstants.*;
import static io.alw.css.fosimulator.template.domain.CashLegType.*;

public final class MmTemplate extends CashMessageTemplateWithDataStore<MmTradeContext> {
    private final CashMessageStoreHelper<MmTradeContext> msgStoreHelper;

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);

        CashMessageStore<MmTradeContext> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    @Override
    protected Predicate<MmTradeContext> tradeContextAmendmentFrequency() {
        return _ -> rndm.nextInt(0, 100) > 80;
    }

    @Override
    protected TradeEventActionPair getNextEventActionPair(TradeEventType amendMsgEvt, TradeEventAction amendMsgAct) {

    }

    /// Build new template for MM cashflow. A new MM trade can have 1 to 3 cashMessages depending on whether its a TERM or CALL and depending on the interest cashflow
    @Override
    public MmTemplate withRootTemplateValues() {
        // Create MessageContext
        MmTradeContext trdCtx = createTradeContext();
        // Create Ids for PRINCIPAL, INTEREST and if applicable for MATURITY as well
        Map<CashLegType, Ids> idsMap = createFirstVersionIds(trdCtx);
        // Build PRINCIPAL leg of the MoneyMarket trade with base values
        FoCashMessageBuilder bdr = getBaseCashMsgBuilder(idsMap.get(MM_PRINCIPAL), trdCtx);
        // Set values specific to the PRINCIPAL leg
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound)))
        ;

        // Set tradeLinks on the PRINCIPAL leg
        MmCashLeg maturityLeg = trdCtx.maturityLeg();
        InterestCashLeg interestLeg = trdCtx.interestLegs().getFirst();

        // IMPORTANT NOTE: The order here is important.
        // MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
        // The lambdas added via [io.alw.datagen.template.AggregateTemplateBuilder#withGroupedItem(Supplier)] method will be executed strictly in the same order as they are inserted in the queue
        if (maturityLeg != null && interestLeg != null) {
            buildMaturityAndInterestLeg(trdCtx, maturityLeg, idsMap.get(MM_MATURITY), interestLeg, idsMap.get(MM_INTEREST));
        } else if (maturityLeg != null) {
            this.withGroupedItem(maturityLeg::setCashMessage, () -> buildMaturityLeg(trdCtx, idsMap.get(MM_MATURITY)));
        } else if (interestLeg != null) {
            this.withGroupedItem(interestLeg::setCashMessage, () -> buildInterestLeg(trdCtx, idsMap.get(MM_INTEREST)));
        }

        return this;
    }

    @Override
    protected void buildCashMessageAmendmentContext(Consumer<CashMessageAmendmentContext> amendedMessageBuilderFunc, MmTradeContext trdCtxForAmendment) {
        switch (cyclicAmendableMmLegProvider.get()) {
            case MM_PRINCIPAL -> {
                var msgAmendCtx = buildCashMessageAmendmentContextStep2(trdCtxForAmendment.principalLeg(), trdCtxForAmendment);
                amendedMessageBuilderFunc.accept(msgAmendCtx);
            }
            case MM_MATURITY -> {
                MmCashLeg maturityLeg = trdCtxForAmendment.maturityLeg();
                if (maturityLeg != null && maturityLeg.cashMessage() != null) {
                    var msgAmendCtx = buildCashMessageAmendmentContextStep2(maturityLeg, trdCtxForAmendment);
                    amendedMessageBuilderFunc.accept(msgAmendCtx);
                }
            }
            case MM_INTEREST -> throw new RuntimeException("Amending interest leg is not allowed");
            default -> throw new RuntimeException("Invalid cash leg type for MM Trade");
        }
    }

    private CashMessageAmendmentContext buildCashMessageAmendmentContextStep2(MmCashLeg primaryAmendmentSubject, MmTradeContext trdCtxForAmendment) {
        var primaryAmndSubMsg = trdCtxForAmendment.principalLeg().cashMessage();
        var nextTradeEventAction = getNextEventActionPair(primaryAmndSubMsg.tradeEventType(), primaryAmndSubMsg.tradeEventAction());
        var nextEventType = nextTradeEventAction.event();
        return switch (nextEventType) {
            // Common trade amend events applicable for all trades
            case CANCEL -> buildAmendmentContextForCancelEvent(nextTradeEventAction);
            case AMEND, REBOOK -> buildAmendmentContextForCommonAmendEvents(primaryAmendmentSubject.cashLegType(), trdCtxForAmendment, nextTradeEventAction);

            case BOOK_MOVE -> throw new RuntimeException("Trade amendment for BOOK_MOVE event is not implemented yet");
            // Trade specific amendments
            case TERMINATE, ROLL, EXERCISE, CORRECTION, KNOCK_OUT, EXPIRE, FIX, UN_FIX, INTEREST_ACTION, MATURE ->
                    throw new RuntimeException("Trade amendment trade specific event: " + nextEventType + " are not implemented yet");
            case NEW_TRADE -> throw new RuntimeException("Invalid selection of next trade event and action. NEW_TRADE is not amendment");
        };
    }

    /// NOTE: This method is used to handle only cancel event, [TradeEventType#CANCEL], for both [CashLegType#MM_PRINCIPAL] and [CashLegType#MM_MATURITY]
    private CashMessageAmendmentContext buildAmendmentContextForCancelEvent(TradeEventActionPair nextTradeEventAction) {
        // No field need to be amended because this is trade cancellation
        return new CashMessageAmendmentContext(nextTradeEventAction);
    }

    /// NOTE: This method is used to handle common amendment events, [TradeEventType#AMEND] and [TradeEventType#REBOOK], for both [CashLegType#MM_PRINCIPAL] and [CashLegType#MM_MATURITY]
    private CashMessageAmendmentContext buildAmendmentContextForCommonAmendEvents(CashLegType primaryAmendmentSubjectCashLegType, MmTradeContext trdCtx, TradeEventActionPair nextTradeEventAction) {
        MmCashLeg principalLeg = trdCtx.principalLeg();
        MmCashLeg maturityLeg = trdCtx.maturityLeg(); // NOTE: MaturityLeg could be null for MM CALL trades

        Set<AmendableFoCashMessageFieldType> amendableFieldTypes = cyclicAmendableFoCashMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        // NOTE: To determine new amount for InterestLeg, the amended amount and amended valueDate of both *principalLeg* and *maturityLeg* are needed, although they may not be computed yet during execution
        // One of the `AmendableFoCashMessageFieldSupplier` type is used to lazily obtain the amendable fields after the amended principalLeg and maturityLeg are built.
        var intLegAmndFieldSupplier = new AmendableFoCashMessageFieldSupplier.SupplierWithMessageSelector(trdCtx, givenTrdCtx -> ((MmTradeContext) givenTrdCtx).interestLegs().stream().filter(cl -> cl.cashMessage().valueDate().isAfter(msgTemplateHelper.currentDateForMsgTemplate())).toList());
        for (var ft : amendableFieldTypes) {
            switch (ft) {
                case AMOUNT -> {
                    var amount = MmAmendmentFieldValue.PrimarySubject.forAmount(rndm);
                    intLegAmndFieldSupplier.add(cl -> new AmendableFoCashMessageField.Amount(determineInterestLegAmount(principalLeg, maturityLeg, (InterestCashLeg) cl)));
                    amendableFields
                            .add(MM_PRINCIPAL, amount)
                            .add(MM_MATURITY, amount) // The Pay/Receive direction remains the same
                            .add(MM_INTEREST, intLegAmndFieldSupplier);
                }
                case COUNTERPARTY_CODE -> {
                    var cpCode = MmAmendmentFieldValue.PrimarySubject.forCounterpartyCode(principalLeg, msgTemplateHelper);
                    amendableFields
                            .add(MM_PRINCIPAL, cpCode)
                            .add(MM_MATURITY, cpCode)
                            .add(MM_INTEREST, intLegAmndFieldSupplier.add(_ -> cpCode));
                }
                case VALUE_DATE -> {
                    if (primaryAmendmentSubjectCashLegType == MM_PRINCIPAL) {
                        // No adjustments not needed for MM_INTEREST with respect to valueDate change of principalLeg

                        // Get new valueDate for principalLeg and add to the collection
                        var principalLegNewVd = MmAmendmentFieldValue.PrimarySubject.PrincipalLeg.forValueDate(principalLeg, msgTemplateHelper, trdCtx);
                        amendableFields.add(MM_PRINCIPAL, principalLegNewVd);

                        // Conditionally apply new valueDate for maturityLeg
                        if (maturityLeg != null) {
                            // Condition
                            Predicate<CashLeg> matLegVdAmendmentCondition = ml ->
                                    ml != null && ml.cashMessage() != null
                                            && principalLeg.cashMessage().valueDate().until(ml.cashMessage().valueDate(), ChronoUnit.DAYS) < 10;

                            // Create conditional valueDate supplier for maturityLeg and add to the collection
                            var matLegVdConditionalSupplier = new AmendableFoCashMessageFieldSupplier
                                    .ConditionalSupplier(maturityLeg, matLegVdAmendmentCondition)
                                    .add(givenMatCashLeg -> {
                                        // Determine new valueDate for maturityLeg
                                        var principalLegOldVd = principalLeg.cashMessage().valueDate();
                                        long daysDiff = principalLegOldVd.until(principalLegNewVd.date(), ChronoUnit.DAYS);
                                        LocalDate newMatValueDate = givenMatCashLeg.cashMessage().valueDate().plusDays(daysDiff);
                                        return new AmendableFoCashMessageField.ValueDate(newMatValueDate);
                                    });

                            amendableFields.add(MM_MATURITY, matLegVdConditionalSupplier);
                        }
                    } else if (primaryAmendmentSubjectCashLegType == MM_MATURITY && maturityLeg != null) {
                        // Determine new valueDate for maturityLeg and add to the collection
                        LocalDate newValueDate = determineMaturityLegValueDate(maturityLeg);
                        amendableFields.add(MM_MATURITY, new AmendableFoCashMessageField.ValueDate(newValueDate));
                        // NOTE: No adjustments to any other cash legs are required when valueDate of maturityLeg is amended
                    }
                }
            }
        }

        // 1. Amendment context for PrincipalLeg(primary amendment subject)
        var primaryAmndSubCtx = new AmendmentSubjectContextEager(principalLeg, principalLeg::setCashMessage, Collections.unmodifiableSet(amendableFields.get(MM_PRINCIPAL)));
        // 2. Amendment context for MaturityLeg
        var maturityAmndSubCtx = new AmendmentSubjectContextLazy(cl -> cl::setCashMessage, amendableFields.get(MM_MATURITY));
        // 3. Amendment context for InterestLegs
        var interestAmndCtx = new AmendmentSubjectContextLazy(cl -> cl::setCashMessage, amendableFields.get(MM_INTEREST));

        return new CashMessageAmendmentContext(nextTradeEventAction)
                .addNextAmndSubCtx(primaryAmndSubCtx)
                .addNextAmndSubCtx(maturityAmndSubCtx)
                .addNextAmndSubCtx(interestAmndCtx);
    }

    /// IMPORTANT NOTE: The order here is important.
    /// MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
    /// The lambdas added via [AggregateTemplateBuilder#withGroupedItem] method will be executed strictly in the same order as they are inserted in the queue
    private void buildMaturityAndInterestLeg(MmTradeContext trdCtx, MmCashLeg maturityLeg, Ids maturityLegIds, InterestCashLeg interestLeg, Ids interestLegIds) {
        // 1. Add building function of MATURITY leg
        this.withGroupedItem(maturityLeg::setCashMessage, () -> buildMaturityLeg(trdCtx, maturityLegIds));
        // 2. Add building function of INTEREST leg
        this.withGroupedItem(interestLeg::setCashMessage, () -> buildInterestLeg(trdCtx, interestLegIds));
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    private FoCashMessageBuilder buildInterestLeg(MmTradeContext trdCtx, Ids interestLegIds) {
        var principalLeg = trdCtx.principalLeg();
        var maturityLeg = trdCtx.maturityLeg(); // NOTE: the 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
        var principalCashMsg = principalLeg.cashMessage();
        InterestCashLeg interestLeg = trdCtx.interestLegs().getFirst();

        // Build the INTEREST leg
        var bdr = createBuilderFrom(principalLeg, MM_INTEREST)
                // Id and version of INTEREST leg was already determined when the PRINCIPAL was created
                .cashflowID(interestLegIds.cashflowID())
                .cashflowVersion(interestLegIds.cashflowVersion())
                .tradeID(interestLegIds.tradeID())
                .tradeVersion(interestLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(principalLeg, interestLeg, maturityLeg))
                .payOrReceive(principalCashMsg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(determineInterestLegAmount(principalLeg, maturityLeg, interestLeg));

        if (interestLeg.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    /// NOTE: The amount returned is not a result of a proper calculation based on rate.
    /// It is just a meaningful enough number for an interest leg when the following parameters are taken into consideration:
    /// - principalAmount, interest basis, interest payout frequency, rate type and maturity date
    ///
    /// This is done solely to avoid calculations using BigDecimal. Actual amount is not required.
    private BigDecimal determineInterestLegAmount(CashLeg principalLeg, CashLeg maturityLeg, InterestCashLeg interestCashLeg) {
        final FoCashMessage principalCashMsg = principalLeg.cashMessage();
        final LocalDate maturityLegValueDate;
        if (maturityLeg == null || maturityLeg.cashMessage() == null) {
            maturityLegValueDate = principalCashMsg.valueDate().plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
        } else {
            maturityLegValueDate = maturityLeg.cashMessage().valueDate();
        }

        // Re-use the interest amount if it was already determined, but only if the values used to determine has not changed
        var intrLegCtx = interestCashLeg.interestLegContext();
        if (intrLegCtx != null) {
            var lastUsedInterestAmount = intrLegCtx.lastUsedInterestAmount();
            var lastUsedPrincipalAmount = intrLegCtx.lastUsedPrincipalAmount();
            var lastUsedPrincipalValueDate = intrLegCtx.lastUsedPrincipalValueDate();
            var lastUsedMaturityValueDate = intrLegCtx.lastUsedMaturityValueDate();

            if (lastUsedPrincipalAmount.equals(principalCashMsg.amount())
                    && lastUsedPrincipalValueDate.equals(principalCashMsg.valueDate())
                    && lastUsedMaturityValueDate.equals(maturityLegValueDate)) {
                return switch (interestCashLeg.rateType()) {
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
        var principalAmount = principalCashMsg.amount();
        var principalValueDate = principalCashMsg.valueDate();
        var newInterestAmount = switch (interestCashLeg.interestBasis()) {
            case ThirtyBy360 -> {
                long numOfDays = ChronoUnit.DAYS.between(principalValueDate, maturityLegValueDate);
                double tenPercentOfPrincipal = (principalAmount.doubleValue() / 100) * 10;

                yield switch (interestCashLeg.ipFrequency()) {
                    case DAY -> BigDecimal.valueOf(tenPercentOfPrincipal / (double) numOfDays);
                    case MONTHLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 30));
                    case QUARTERLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 90));
                    case SEMI_ANNUALLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 180));
                    case YEARLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 360));
                    case PRINCIPAL_MATURITY -> BigDecimal.valueOf(tenPercentOfPrincipal);
                };
            }
        };

        interestCashLeg.setInterestLegContext(new InterestLegContext(newInterestAmount, principalAmount, principalValueDate, maturityLegValueDate));
        return newInterestAmount;
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    private LocalDate determineInterestLegValueDate(CashLeg principalLeg, MmMetadata interestMetadata, CashLeg maturityLeg) {
        final LocalDate principalLegValueDate = principalLeg.cashMessage().valueDate();
        final LocalDate maturityLegValueDate;
        if (maturityLeg == null || maturityLeg.cashMessage() == null) {
            maturityLegValueDate = principalLegValueDate.plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
        } else {
            maturityLegValueDate = maturityLeg.cashMessage().valueDate();
        }


        return switch (interestMetadata.interestBasis()) {
            case ThirtyBy360 -> switch (interestMetadata.ipFrequency()) {
                case DAY -> msgTemplateHelper.getFutureValueDate(1, principalLegValueDate, maturityLegValueDate);
                case MONTHLY -> msgTemplateHelper.getFutureValueDate(30, principalLegValueDate, maturityLegValueDate);
                case QUARTERLY -> msgTemplateHelper.getFutureValueDate(90, principalLegValueDate, maturityLegValueDate);
                case SEMI_ANNUALLY -> msgTemplateHelper.getFutureValueDate(180, principalLegValueDate, maturityLegValueDate);
                case YEARLY -> msgTemplateHelper.getFutureValueDate(360, principalLegValueDate, maturityLegValueDate);
                case PRINCIPAL_MATURITY -> maturityLegValueDate;
            };
        };
    }

    private FoCashMessageBuilder buildMaturityLeg(MmTradeContext trdCtx, Ids maturityLegIds) {
        var principalLeg = trdCtx.principalLeg();
        var principalMsg = principalLeg.cashMessage();
        var maturityLegValueDate = determineMaturityLegValueDate(principalLeg);

        // Build the MATURITY leg
        var bdr = createBuilderFrom(principalLeg, MM_MATURITY)
                // Id and version of MATURITY leg was already determined when the PRINCIPAL was created
                .cashflowID(maturityLegIds.cashflowID())
                .cashflowVersion(maturityLegIds.cashflowVersion())
                .tradeID(maturityLegIds.tradeID())
                .tradeVersion(maturityLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(maturityLegValueDate)
                .payOrReceive(principalMsg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(principalMsg.amount().negate());

        if (principalLeg.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    private LocalDate determineMaturityLegValueDate(MmCashLeg relativeCashLeg) {
        return msgTemplateHelper.getRndmFutureValueDateRelativeTo(relativeCashLeg.cashMessage().valueDate(), false, 10);
    }

    private Map<CashLegType, Ids> createFirstVersionIds(MmTradeContext trdCtx) {
        var principalLegIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowAndVersionOneTrade(MM_PRINCIPAL);
        var interestLegIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowFromExistingTrade(MM_INTEREST, principalLegIds);

        if (trdCtx.rootFoCashMessage().tradeType() != TradeType.MM_CALL) {
            var maturityLegIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowFromExistingTrade(MM_MATURITY, principalLegIds);
            return Map.of(MM_PRINCIPAL, principalLegIds, MM_INTEREST, interestLegIds, MM_MATURITY, maturityLegIds);
        } else {
            return Map.of(MM_PRINCIPAL, principalLegIds, MM_INTEREST, interestLegIds);
        }
    }

    /// Interest Basis is always assigned a constant: InterestBasis.ThirtyBy360. Corresponding calculation for other basis types are not implemented.
    /// Interest cashflow should be generated based on [InterestPayoutFrequency]
    /// Returns message contexts for cashflow version 1. The method parameter `principalLegBuilder` must be of cashflow version 1
    private MmTradeContext createTradeContext() {
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = InterestBasis.ThirtyBy360;

        var principal = new MmCashLeg(MM_PRINCIPAL, rateType, ipFrequency, basis);
        var interest = new InterestCashLeg(MM_INTEREST, rateType, ipFrequency, basis);
        var interests = new ArrayList<InterestCashLeg>();
        interests.add(interest);

        return switch (this.tradeType) {
            case MM_TERM -> {
                var maturity = new MmCashLeg(MM_MATURITY, rateType, ipFrequency, basis);
                yield new MmTradeContext(principal, interests, maturity);
            }
            case MM_CALL -> new MmTradeContext(principal, interests);
            default -> throw new IllegalStateException("Invalid TradeType: " + this.tradeType + " for an MmTemplate");
        };
    }

    @Override
    protected CashMessageStoreHelper<MmTradeContext> msgStoreHelper() {
        return msgStoreHelper;
    }

    private static final class MmAmendmentFieldValue {
        private static final class PrimarySubject {
            // Common methods for every PrimarySubject
            private static AmendableFoCashMessageField forAmount(RandomGenerator rndm) {
                var newAmount = BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound));
                return new AmendableFoCashMessageField.Amount(newAmount);
            }

            private static AmendableFoCashMessageField forCounterpartyCode(CashLeg subjectCashLeg, CashMessageTemplateHelper msgTemplateHelper) {
                FoCashMessage cashMsg = subjectCashLeg.cashMessage();
                String newCounterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(cashMsg.counterpartyCode());
                return new AmendableFoCashMessageField.CounterpartyCode(newCounterpartyCode);
            }

            private static final class PrincipalLeg {
                private static AmendableFoCashMessageField.ValueDate forValueDate(CashLeg principalCashLeg, CashMessageTemplateHelper msgTemplateHelper, MmTradeContext trdCtx) {
                    var principalCashMsg = principalCashLeg.cashMessage();
                    var currentDate = msgTemplateHelper.currentDateForMsgTemplate();
                    var maturityLeg = trdCtx.maturityLeg();
                    final LocalDate maturityLegValueDate;
                    if (maturityLeg == null || maturityLeg.cashMessage() == null) {
                        maturityLegValueDate = principalCashMsg.valueDate().plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
                    } else {
                        maturityLegValueDate = maturityLeg.cashMessage().valueDate();
                    }
                    // New valueDate for principal leg
                    LocalDate newValueDate = msgTemplateHelper.getFutureValueDate(1, currentDate, maturityLegValueDate);
                    return new AmendableFoCashMessageField.ValueDate(newValueDate);
                }
            }
        }
    }
}
