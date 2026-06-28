package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegBuilder;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.domain.InterestPayoutFrequency;
import io.alw.css.fosimulator.template.domain.InterestTradeLeg;
import io.alw.css.fosimulator.template.domain.MmTrade;
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
import static io.alw.css.domain.trade.TradeLegType.*;
import static io.alw.css.fosimulator.template.MmTemplateConstants.*;
import static io.alw.css.fosimulator.template.domain.InterestBasis.ThirtyBy360;

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
        MmTrade extTrd = createExtendedTrade();
        // Create MoneyMarket trade builder with base values
        createNewTradeWithDefaultValues(extTrd);
        // Build PRINCIPAL leg
        this.withChildTemplateDirective(extTrd::setRootTradeLeg, this::buildPrincipalLeg);

        // IMPORTANT NOTE: The order here is important.
        // MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
        // The lambdas added via [io.alw.datagen.template.AggregateTemplateBuilder#withGroupedItem(Supplier)] method will be executed strictly in the same order as they are inserted in the queue
        var newTrdEventAndAction = new TradeEventActionPair(TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        switch (tradeType()) {
            case MM_TERM -> {
                var interestLegIds = new Id(extTrd.nextTradeLegId(), VERSION_ONE);
                var maturityLegIds = new Id(extTrd.nextTradeLegId(), VERSION_ONE);
                buildMaturityAndInterestLeg(extTrd, maturityLegIds, interestLegIds, newTrdEventAndAction);
            }
            case MM_CALL -> {
                var interestLegIds = new Id(extTrd.nextTradeLegId(), VERSION_ONE);
                this.withChildTemplateDirective(() -> createInterestTradeLeg(extTrd, interestLegIds, newTrdEventAndAction));
            }
            default -> throw new RuntimeException("Invalid TradeType for MmTemplate");
        }
        return this;
    }

    private TradeLegBuilder buildPrincipalLeg() {
        return createNewTradeLegWithDefaultValues(extTrd(), MM_PRINCIPAL)
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound)))
                ;
    }

    @Override
    protected void buildCashMessageAmendmentContext(Consumer<TradeAmendmentContext> amendmentMessageBuilderFunc, MmTrade trdForAmendment) {
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

    private TradeAmendmentContext buildCashMessageAmendmentContextStep2(TradeLeg primaryAmendmentSubject, MmTrade trd) {
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
    private TradeAmendmentContext buildAmendmentContextForCommonAmendEvents(TradeLegType primaryAmendmentSubjectTradeLegType, MmTrade trd, TradeEventActionPair nextTradeEventAction) {
        var principalLeg = trd.principalLeg();
        var maturityLeg = trd.maturityLeg(); // NOTE: MaturityLeg could be null for MM CALL trades

        Set<AmendableFoCashMessageFieldType> amendableFieldTypes = cyclicAmendableFoCashMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        // NOTE: To determine new amount for InterestLeg, the amended amount and amended valueDate of both *principalLeg* and *maturityLeg* are needed, although they may not be computed yet during execution
        // One of the `AmendableFoCashMessageFieldSupplier` type is used to lazily obtain the amendable fields after the amended principalLeg and maturityLeg are built.
        var intLegAmndFieldSupplier = new AmendableFieldSupplier.SupplierWithMessageSelector(trd, extTrd -> ((MmTrade) extTrd).interestLegs().stream().filter(itl -> itl.interestLeg().valueDate().isAfter(msgTemplateHelper.currentDateForMsgTemplate())).toList());
        for (var ft : amendableFieldTypes) {
            switch (ft.amendmentTarget()) {
                case TRADE -> {
                    switch (ft) {
                        case COUNTERPARTY_CODE, VALUE_DATE, AMOUNT ->
                                throw new RuntimeException("CounterpartyCode, ValueDate and Amount amendments are not possible on Trade level. Instead, it must be done on TradeLeg level");
                    }
                }
                case TRADE_LEG -> {
                    switch (ft) {
                        case COUNTERPARTY_CODE -> {
                            var cpCode = MmAmendmentFieldValue.PrimarySubject.forCounterpartyCode(principalLeg, msgTemplateHelper);
                            amendableFields
                                    .addForTradeLeg(MM_PRINCIPAL, cpCode)
                                    .addForTradeLeg(MM_INTEREST, intLegAmndFieldSupplier.add(_ -> cpCode));
                            if (maturityLeg != null) {
                                amendableFields.addForTradeLeg(MM_MATURITY, cpCode);
                            }
                        }
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
                                    Predicate<TradeDetail> matLegVdAmendmentCondition = ml ->
                                            ml != null
                                                    && principalLeg.valueDate().until(((TradeLeg) ml).valueDate(), ChronoUnit.DAYS) < 10;

                                    // Create conditional valueDate supplier for maturityLeg and add to the collection
                                    var matLegVdConditionalSupplier = new AmendableFieldSupplier
                                            .ConditionalSupplier(maturityLeg, matLegVdAmendmentCondition)
                                            .add(givenMatCashLeg -> {
                                                // Determine new valueDate for maturityLeg
                                                var principalLegOldVd = principalLeg.valueDate();
                                                long daysDiff = principalLegOldVd.until(principalLegNewVd.date(), ChronoUnit.DAYS);
                                                LocalDate newMatValueDate = ((TradeLeg) givenMatCashLeg).valueDate().plusDays(daysDiff);
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
        var amndCtx = new TradeAmendmentContext(nextTradeEventAction, tradeLevelAmendableFields, trd::setTrade);

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
        var interestAmndCtx = new TradeLegAmendmentContextLazy(trdDetail -> (trdLeg) -> ((InterestTradeLeg) trdDetail).setInterestLeg(trdLeg), fieldsForInterestLegs);
        amndCtx.addNextTradeLegAmndCtx(interestAmndCtx);

        return amndCtx;
    }

    /// IMPORTANT NOTE: The order here is important.
    /// MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
    /// The lambdas added via [AggregateTemplateBuilder#withGroupedItem(Consumer, Supplier)] method will be executed strictly in the same order as they are inserted in the queue
    private void buildMaturityAndInterestLeg(MmTrade extTrade, Id maturityLegId, Id interestLegId, TradeEventActionPair trdEventAndAction) {
        // 1. Add building function of MATURITY leg to the builder
        this.withChildTemplateDirective(extTrade::setMaturityLeg, () -> buildMaturityLeg(extTrade, maturityLegId, trdEventAndAction));
        // 2. create InterestTradeLeg object and add the interestLeg building function to the builder
        this.withChildTemplateDirective(() -> createInterestTradeLeg(extTrade, interestLegId, trdEventAndAction));
    }

    private void createInterestTradeLeg(MmTrade extTrd, Id interestLegId, TradeEventActionPair trdEventAndAction) {
        var principalLeg = extTrd.principalLeg();
        var maturityLeg = extTrd.maturityLeg(); // NOTE: the 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
        // Create InterestTradeLeg object
        InterestTradeLeg newIntrTrdLegObj = createInterestTradeLegAndAssociateWithMmTrade(extTrd, principalLeg, maturityLeg);
        // Add building function of INTEREST leg to the builder
        this.withChildTemplateDirective(newIntrTrdLegObj::setInterestLeg, () -> buildInterestLeg(extTrd, interestLegId, trdEventAndAction, newIntrTrdLegObj));
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    /// This method is intended to be used not only for the first interest leg but also for future interest legs. Therefor Ids are received as a parameter. The same pattern is followed to build maturity leg although there can be only one maturityLeg
    private TradeLegBuilder buildInterestLeg(MmTrade extTrd, Id interestLegId, TradeEventActionPair trdEventAndAction, InterestTradeLeg newIntrTrdLegObj) {
        var principalLeg = extTrd.principalLeg();
        var maturityLeg = extTrd.maturityLeg(); // NOTE: the 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront

        // Create the INTEREST leg builder
        var bdr = TradeLegBuilder.builder(principalLeg)
                .tradeLegId(interestLegId.Id())
                .tradeLegVersion(interestLegId.version())
                .tradeLegType(MM_INTEREST)
                .tradeEventType(trdEventAndAction.event())
                .tradeEventAction(trdEventAndAction.action())
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(extTrd, principalLeg, maturityLeg))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(newIntrTrdLegObj.interestLegContext().lastUsedInterestAmount())
                // no change for currCode
                ;

        if (extTrd.rateType() == FLOAT && (VERSION_ONE != bdr.tradeLegVersion() || VERSION_ONE != extTrd.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    private InterestTradeLeg createInterestTradeLegAndAssociateWithMmTrade(MmTrade extTrd, TradeLeg principalLeg, TradeLeg maturityLeg) {
        final BigDecimal interestAmount;
        if (!extTrd.interestLegs().isEmpty()) {
            final InterestTradeLeg mostRecentInterestLeg = extTrd.interestLegs().getLast();
            interestAmount = determineInterestLegAmount(extTrd, principalLeg, maturityLeg, mostRecentInterestLeg);
        } else {
            interestAmount = createInterestLegAmount(extTrd, principalLeg, maturityLeg);
        }

        BigDecimal principalAmount = principalLeg.amount();
        LocalDate principalValueDate = principalLeg.valueDate();
        LocalDate maturityLegValueDate = getRealOrPotentialMaturityLegValueDate(principalLeg, maturityLeg);

        // Create the InterestLegContext with the above created interest amount
        var interestLegCtx = new InterestLegContext(interestAmount, principalAmount, principalValueDate, maturityLegValueDate);
        // Create the InterestTradeLeg object and associate with the MMTrade
        var intTrdLeg = new InterestTradeLeg(interestLegCtx);
        extTrd.addInterestLeg(intTrdLeg);

        return intTrdLeg;
    }

    private BigDecimal createInterestLegAmount(MmTrade extTrd, TradeLeg principalLeg, TradeLeg maturityLeg) {
        LocalDate maturityLegValueDate = getRealOrPotentialMaturityLegValueDate(principalLeg, maturityLeg);

        // Determine the interest amount
        var principalAmount = principalLeg.amount();
        var principalValueDate = principalLeg.valueDate();
        return switch (extTrd.interestBasis()) {
            case ThirtyBy360 -> {
                long numOfDays = ChronoUnit.DAYS.between(principalValueDate, maturityLegValueDate);
                double tenPercentOfPrincipal = (principalAmount.doubleValue() / 100) * 10;

                yield switch (extTrd.ipFrequency()) {
                    case DAY -> BigDecimal.valueOf(tenPercentOfPrincipal / (double) numOfDays);
                    case MONTHLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 30));
                    case QUARTERLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 90));
                    case SEMI_ANNUALLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 180));
                    case YEARLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 360));
                    case PRINCIPAL_MATURITY -> BigDecimal.valueOf(tenPercentOfPrincipal);
                };
            }
        };
    }

    /// NOTE: The amount returned is not a result of a proper calculation based on rate.
    /// It is just a meaningful enough number for an interest leg when the following parameters are taken into consideration:
    /// - principalAmount, interest basis, interest payout frequency, rate type and maturity date
    ///
    /// This is done solely to avoid calculations using BigDecimal. Actual amount is not required.
    private BigDecimal determineInterestLegAmount(MmTrade extTrd, TradeLeg principalLeg, TradeLeg maturityLeg, InterestTradeLeg interestLeg) {
        LocalDate maturityLegValueDate = getRealOrPotentialMaturityLegValueDate(principalLeg, maturityLeg);

        // Re-use the interest amount if it was already determined, but only if the values used to determine has not changed
        var intrLegCtx = interestLeg.interestLegContext();
        var lastUsedInterestAmount = intrLegCtx.lastUsedInterestAmount();
        var lastUsedPrincipalAmount = intrLegCtx.lastUsedPrincipalAmount();
        var lastUsedPrincipalValueDate = intrLegCtx.lastUsedPrincipalValueDate();
        var lastUsedMaturityValueDate = intrLegCtx.lastUsedMaturityValueDate();

        if (lastUsedPrincipalAmount.equals(principalLeg.amount())
                && lastUsedPrincipalValueDate.equals(principalLeg.valueDate())
                && (maturityLeg == null || lastUsedMaturityValueDate.equals(maturityLegValueDate))) {
            return switch (extTrd.rateType()) {
                case FIXED -> lastUsedInterestAmount;
                case FLOAT -> {
                    BigDecimal newIntAmt = BigDecimal.valueOf(lastUsedInterestAmount.doubleValue() + 158);// Even though rate decrease the amount increases. This is ok! Actual amount is not required
                    intrLegCtx.setLastUsedInterestAmount(newIntAmt);
                    yield newIntAmt;
                }
            };
        } else {
            return createInterestLegAmount(extTrd, principalLeg, maturityLeg);
        }
    }

    /// If MATURITY leg exists, returns the actual maturityLeg valueDate. If not, returns a potential valueDate as the maturityLeg valueDate
    private LocalDate getRealOrPotentialMaturityLegValueDate(TradeLeg principalLeg, TradeLeg maturityLeg) {
        final LocalDate maturityLegValueDate;
        if (maturityLeg == null) {
            maturityLegValueDate = principalLeg.valueDate().plusDays(msgTemplateHelper.currentDayForMsgTemplate() + 360);
        } else {
            maturityLegValueDate = maturityLeg.valueDate();
        }

        return maturityLegValueDate;
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

    private TradeLegBuilder buildMaturityLeg(MmTrade trd, Id maturityLegId, TradeEventActionPair trdEventAndAction) {
        var principalLeg = trd.principalLeg();
        var maturityLegValueDate = determineMaturityLegValueDate(principalLeg);

        // Create MATURITY leg builder
        var bdr = TradeLegBuilder.builder(principalLeg)
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
    @Override
    protected MmTrade createExtendedTrade() {
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = ThirtyBy360;

        return new MmTrade(rateType, ipFrequency, basis);
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
            private static AmendableField forAmount(RandomGenerator rndm) {
                var newAmount = BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound));
                return new AmendableField.Amount(newAmount);
            }

            private static AmendableField forCounterpartyCode(TradeLeg trdLeg, CashMessageTemplateHelper msgTemplateHelper) {
                String newCounterpartyCode = msgTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(trdLeg.counterpartyCode());
                return new AmendableField.CounterpartyCode(newCounterpartyCode);
            }

            private static final class PrincipalLeg {
                private static AmendableField.ValueDate forValueDate(TradeLeg principalLeg, CashMessageTemplateHelper msgTemplateHelper, MmTrade trdCtx) {

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
