package io.alw.css.tradepublisher.trade.template;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegBuilder;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.tradepublisher.generator.DayTicker;
import io.alw.css.tradepublisher.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.store.InMemoryStore;
import io.alw.css.tradepublisher.store.Store;
import io.alw.css.tradepublisher.store.StoreHelper;
import io.alw.css.tradepublisher.trade.model.Entity;
import io.alw.css.tradepublisher.trade.model.TradeEventActionPair;
import io.alw.css.tradepublisher.trade.service.RefDataService;
import io.alw.css.tradepublisher.trade.template.domain.InterestPayoutFrequency;
import io.alw.css.tradepublisher.trade.template.domain.InterestTradeLeg;
import io.alw.css.tradepublisher.trade.template.domain.MmTrade;
import io.alw.css.tradepublisher.trade.template.domain.TradeLegGenerationSchedule;
import io.alw.css.tradepublisher.trade.template.model.*;
import io.alw.datagen.template.AggregateTemplateBuilder;
import io.alw.datagen.template.ChildBuildDirective;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.common.PayOrReceive.PAY;
import static io.alw.css.domain.common.PayOrReceive.RECEIVE;
import static io.alw.css.domain.common.RateType.FLOAT;
import static io.alw.css.domain.trade.TradeLegType.*;
import static io.alw.css.tradepublisher.trade.template.MmTemplateConstants.principalLegAmountBound;
import static io.alw.css.tradepublisher.trade.template.MmTemplateConstants.principalLegAmountOrigin;
import static io.alw.css.tradepublisher.trade.template.domain.InterestBasis.ThirtyBy360;
import static io.alw.css.tradepublisher.trade.template.domain.InterestPayoutFrequency.*;

public final class MmTemplate extends TradeLegGeneratingTemplate<MmTrade, MmTemplate> {
    private final StoreHelper<MmTrade> trdStoreHelper;
    private final MmTemplateConstants mmTemplateConstants;

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, TradeTemplateProperties trdTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, trdTemplateProps);

        Store<MmTrade> trdStore = new InMemoryStore<>();
        this.trdStoreHelper = new StoreHelper<>(dayTicker, trdStore, rndm);
        mmTemplateConstants = new MmTemplateConstants();
    }

    /// Build new template for MM trade. A new MM trade can have 1 to 3 trade legs depending on whether it is a TERM or CALL and depending on the interest tradeLeg
    @Override
    public MmTemplate withRootTemplateValues() {
        // Create MessageContext
        MmTrade extTrd = createExtendedTrade();
        // Create MoneyMarket trade builder with base values
        createNewTradeWithDefaultValues(extTrd);
        // Build PRINCIPAL leg
        this.withChildTemplateDirective(extTrd::setRootTradeLeg, this::createPrincipalLegBuilder);

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
                var directive = createInterestTradeLegBuildDirective(extTrd, interestLegIds, newTrdEventAndAction);
                this.withChildTemplateDirective(directive);
            }
            default -> throw new RuntimeException("Invalid TradeType for MmTemplate");
        }
        return this;
    }

    @Override
    protected List<TradeLegGenerationSchedule> getInitialTradeLegGenerationSchedules(MmTrade mmTrade) {
        return List.of(buildNextTradeLegGenerationScheduleFor(MM_INTEREST, mmTrade));
    }

    @Override
    protected ChildBuildDirective<TradeLeg, TradeLegBuilder, ?> buildTradeLegGenerationDirectiveFromSchedule(MmTrade extTrd, TradeLegGenerationSchedule schedule) {
        return switch (schedule.tradeLegType()) {
            case MM_INTEREST -> {
                var interestLegIds = new Id(extTrd.nextTradeLegId(), VERSION_ONE);
                yield createInterestTradeLegBuildDirective(extTrd, interestLegIds, schedule.tradeEventActionPair());
            }
            case FX_SIDE1, FX_SIDE2, MM_PRINCIPAL, MM_MATURITY, PARENT_TRADE, CHILD_TRADE -> {
                throw new RuntimeException("TradeLeg generation for: " + schedule.tradeLegType() + " is either not permitted or is invalid");
            }
        };
    }

    @Override
    protected TradeLegGenerationSchedule buildNextTradeLegGenerationScheduleFor(TradeLegType tradeLegType, MmTrade mmTrade) {
        return switch (tradeLegType) {
            case MM_INTEREST -> {
                InterestPayoutFrequency ipFrequency = mmTrade.ipFrequency();
                long offestDays = switch (ipFrequency) {
                    case DAY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, YEARLY -> ipFrequency.offsetDays();
                    case PRINCIPAL_MATURITY -> {
                        LocalDate maturityLegValueDate = getRealOrPotentialMaturityLegValueDate(mmTrade.principalLeg(), mmTrade.maturityLeg());
                        yield trdTemplateHelper.currentDateForTrdTemplate().until(maturityLegValueDate, ChronoUnit.DAYS);
                    }
                };

                long scheduleDay = trdTemplateHelper.currentDayForTrdTemplate() + offestDays;
                TradeEventActionPair tradeEventActionPair = new TradeEventActionPair(TradeEventType.INTEREST_ACTION, TradeEventAction.ADD);
                yield new TradeLegGenerationSchedule(scheduleDay, MM_INTEREST, tradeEventActionPair);
            }
            case FX_SIDE1, FX_SIDE2, MM_PRINCIPAL, MM_MATURITY, PARENT_TRADE, CHILD_TRADE -> {
                throw new RuntimeException("TradeLeg generation for: " + tradeLegType + " is either not permitted or is invalid");
            }
        };
    }

    @Override
    protected void buildTradeAmendmentContext(Consumer<TradeAmendmentContext> trdAmendmentBuilderFunc, MmTrade trdForAmendment) {
        switch (mmTemplateConstants.cyclicAmendableMmLegProvider.get()) {
            case MM_PRINCIPAL -> {
                var trdAmendCtx = buildTradeAmendmentContextStep2(trdForAmendment.principalLeg(), trdForAmendment);
                trdAmendmentBuilderFunc.accept(trdAmendCtx);
            }
            case MM_MATURITY -> {
                var maturityLeg = trdForAmendment.maturityLeg();
                // maturityLeg may not be present for CALL trade. If not present, do no amendment to create, hence do not execute the function
                if (maturityLeg != null) {
                    var trdAmendCtx = buildTradeAmendmentContextStep2(maturityLeg, trdForAmendment);
                    trdAmendmentBuilderFunc.accept(trdAmendCtx);
                }
            }
            case MM_INTEREST -> throw new RuntimeException("Amending interest leg is not allowed");
            default -> throw new RuntimeException("Invalid trade leg type for MM Trade");
        }
    }

    private TradeAmendmentContext buildTradeAmendmentContextStep2(TradeLeg primaryAmendmentSubject, MmTrade trd) {
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

        // Get all fields, that require amendment, with their amended value
        Set<AmendableFieldType> amendableFieldTypes = mmTemplateConstants.cyclicAmendableTradeMessageFieldTypeProvider.get();
        var amendableFields = new AmendableFieldsCollection();
        for (var ft : amendableFieldTypes) {
            switch (ft.amendmentTarget()) {
                case TRADE -> buildAmendmentContextForCommonAmendEventsTrade(ft);
                case TRADE_LEG -> buildAmendmentContextForCommonAmendEventsTradeLeg(primaryAmendmentSubjectTradeLegType, trd, ft, amendableFields);
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

    private void buildAmendmentContextForCommonAmendEventsTrade(AmendableFieldType ft) {
        switch (ft) {
            case COUNTERPARTY_CODE, VALUE_DATE, AMOUNT ->
                    throw new RuntimeException("CounterpartyCode, ValueDate and Amount amendments are not possible on Trade level. Instead, it must be done on TradeLeg level");
        }
    }

    private void buildAmendmentContextForCommonAmendEventsTradeLeg(TradeLegType primaryAmendmentSubjectTradeLegType, MmTrade trd, AmendableFieldType amendableFieldType, AmendableFieldsCollection amendableFields) {
        var principalLeg = trd.principalLeg();
        var maturityLeg = trd.maturityLeg(); // NOTE: MaturityLeg could be null for MM CALL trades

        // NOTE: To determine new amount for InterestLeg, the amended amount and amended valueDate of both *principalLeg* and *maturityLeg* are needed, although they may not be computed yet during execution
        // One of the `AmendableTradeMessageFieldSupplier` type is used to lazily obtain the amendable fields after the amended principalLeg and maturityLeg are built.
        var intLegAmndFieldSupplier = new AmendableFieldSupplier.SupplierWithAmendmentSubjectSelector(trd, extTrd -> ((MmTrade) extTrd).interestLegs().stream().filter(itl -> itl.interestLeg().valueDate().isAfter(trdTemplateHelper.currentDateForTrdTemplate())).toList());

        switch (amendableFieldType) {
            case COUNTERPARTY_CODE -> {
                var cpCode = MmAmendmentFieldValue.PrimarySubject.forCounterpartyCode(principalLeg, trdTemplateHelper);
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
                    var principalLegNewVd = MmAmendmentFieldValue.PrimarySubject.PrincipalLeg.forValueDate(principalLeg, trdTemplateHelper, trd);
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
                                .add(givenMatTrdLeg -> {
                                    // Determine new valueDate for maturityLeg
                                    var principalLegOldVd = principalLeg.valueDate();
                                    long daysDiff = principalLegOldVd.until(principalLegNewVd.date(), ChronoUnit.DAYS);
                                    LocalDate newMatValueDate = ((TradeLeg) givenMatTrdLeg).valueDate().plusDays(daysDiff);
                                    return new AmendableField.ValueDate(newMatValueDate);
                                });

                        amendableFields.addForTradeLeg(MM_MATURITY, matLegVdConditionalSupplier);
                    }
                } else if (primaryAmendmentSubjectTradeLegType == MM_MATURITY && maturityLeg != null) {
                    // Determine new valueDate for maturityLeg and add to the collection
                    LocalDate newValueDate = determineMaturityLegValueDate(maturityLeg);
                    amendableFields.addForTradeLeg(MM_MATURITY, new AmendableField.ValueDate(newValueDate));
                    // NOTE: No adjustments to any other trade legs are required when valueDate of maturityLeg is amended
                }
            }
        }
    }

    private TradeLegBuilder createPrincipalLegBuilder() {
        return createNewTradeLegWithDefaultValues(getExtendedTradeOfCurrentBuildCycle(), MM_PRINCIPAL)
                .valueDate(trdTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound)))
                ;
    }

    /// IMPORTANT NOTE: The order here is important.
    /// MaturityLeg must be built before InterestLeg because building InterestLeg requires maturityLegValueDate.
    /// The lambdas added via [AggregateTemplateBuilder#withGroupedItem(Consumer, Supplier)] method will be executed strictly in the same order as they are inserted in the queue
    private void buildMaturityAndInterestLeg(MmTrade extTrade, Id maturityLegId, Id interestLegId, TradeEventActionPair trdEventAndAction) {
        // 1. Add building function of MATURITY leg to the builder
        this.withChildTemplateDirective(extTrade::setMaturityLeg, () -> createMaturityLegBuilder(extTrade, maturityLegId, trdEventAndAction));
        // 2. create InterestTradeLeg build directive and add the directive to the builder
        var directive = createInterestTradeLegBuildDirective(extTrade, interestLegId, trdEventAndAction);
        this.withChildTemplateDirective(directive);

    }

    private ChildBuildDirective<TradeLeg, TradeLegBuilder, ?> createInterestTradeLegBuildDirective(MmTrade extTrd, Id interestLegId, TradeEventActionPair trdEventAndAction) {
        // Create InterestTradeLeg object supplier
        Supplier<InterestTradeLeg> buildStepParamSupplier = () -> {
            var principalLeg = extTrd.principalLeg();
            var maturityLeg = extTrd.maturityLeg(); // NOTE: the 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
            return createInterestTradeLegAndAssociateWithMmTrade(extTrd, principalLeg, maturityLeg);
        };
        // Create interest TradeLeg directive
        Function<InterestTradeLeg, TradeLegBuilder> buildStep = interestTradeLeg -> createInterestLegBuilder(extTrd, interestLegId, trdEventAndAction, interestTradeLeg);
        // Callback to associate TradeLeg to InterestTradeLeg object
        BiConsumer<InterestTradeLeg, TradeLeg> callback = InterestTradeLeg::setInterestLeg;

        return new ChildBuildDirective.ChildBuildDirectiveType3<>(buildStepParamSupplier, buildStep, callback);
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    /// This method is intended to be used not only for the first interest leg but also for future interest legs. Therefor Ids are received as a parameter. The same pattern is followed to build maturity leg although there can be only one maturityLeg
    private TradeLegBuilder createInterestLegBuilder(MmTrade extTrd, Id interestLegId, TradeEventActionPair trdEventAndAction, InterestTradeLeg newIntrTrdLegObj) {
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
            maturityLegValueDate = principalLeg.valueDate().plusDays(trdTemplateHelper.currentDayForTrdTemplate() + 360);
        } else {
            maturityLegValueDate = maturityLeg.valueDate();
        }

        return maturityLegValueDate;
    }

    /// NOTE: The 'maturityLeg' could be null, because for MM CALL trade MaturityLeg is not determined upfront
    private LocalDate determineInterestLegValueDate(MmTrade trd, TradeLeg principalLeg, TradeLeg maturityLeg) {
        final LocalDate principalLegValueDate = principalLeg.valueDate();
        final LocalDate maturityLegValueDate = getRealOrPotentialMaturityLegValueDate(principalLeg, maturityLeg);

        return switch (trd.interestBasis()) {
            case ThirtyBy360 -> switch (trd.ipFrequency()) {
                case DAY -> trdTemplateHelper.getFutureValueDate(DAY.offsetDays(), principalLegValueDate, maturityLegValueDate);
                case MONTHLY -> trdTemplateHelper.getFutureValueDate(MONTHLY.offsetDays(), principalLegValueDate, maturityLegValueDate);
                case QUARTERLY -> trdTemplateHelper.getFutureValueDate(QUARTERLY.offsetDays(), principalLegValueDate, maturityLegValueDate);
                case SEMI_ANNUALLY -> trdTemplateHelper.getFutureValueDate(SEMI_ANNUALLY.offsetDays(), principalLegValueDate, maturityLegValueDate);
                case YEARLY -> trdTemplateHelper.getFutureValueDate(YEARLY.offsetDays(), principalLegValueDate, maturityLegValueDate);
                case PRINCIPAL_MATURITY -> maturityLegValueDate;
            };
        };
    }

    private TradeLegBuilder createMaturityLegBuilder(MmTrade trd, Id maturityLegId, TradeEventActionPair trdEventAndAction) {
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
        return trdTemplateHelper.getRndmFutureValueDateRelativeTo(relativeTradeLeg.valueDate(), false, 10);
    }

    /// Interest Basis is always assigned a constant: InterestBasis.ThirtyBy360. Corresponding calculation for other basis types are not implemented.
    /// Interest trade leg should be generated based on [InterestPayoutFrequency]
    @Override
    protected MmTrade createExtendedTrade() {
        var rateType = mmTemplateConstants.cyclicRateTypeProvider.get();
        var ipFrequency = mmTemplateConstants.cyclicIpFrequencyProvider.get();
        var basis = ThirtyBy360;

        return new MmTrade(rateType, ipFrequency, basis, trdTemplateHelper.currentDayForTrdTemplate());
    }

    @Override
    protected Predicate<MmTrade> amendmentCandidateSelectionCriteriaSecondary() {
        return _ -> rndm.nextInt(0, 100) > 80;
    }

    @Override
    protected TradeEventActionPair determineNextTradeEventAndAction(TradeEventType trdEventType, TradeEventAction trdEventAction) {
        return trdTemplateHelper.determineNextTradeEventAndActionForCommonEvents(rndm, trdEventType, trdEventAction);
    }

    @Override
    protected MmTemplate self() {
        return this;
    }

    @Override
    protected StoreHelper<MmTrade> trdStoreHelper() {
        return trdStoreHelper;
    }

    private static final class MmAmendmentFieldValue {
        private static final class PrimarySubject {
            // Common methods for every PrimarySubject
            private static AmendableField forAmount(RandomGenerator rndm) {
                var newAmount = BigDecimal.valueOf(rndm.nextDouble(principalLegAmountOrigin, principalLegAmountBound));
                return new AmendableField.Amount(newAmount);
            }

            private static AmendableField forCounterpartyCode(TradeLeg trdLeg, TradeTemplateHelper trdTemplateHelper) {
                String newCounterpartyCode = trdTemplateHelper.getCounterpartyCorrespondingToTransactionTypeOtherThan(trdLeg.counterpartyCode());
                return new AmendableField.CounterpartyCode(newCounterpartyCode);
            }

            private static final class PrincipalLeg {
                private static AmendableField.ValueDate forValueDate(TradeLeg principalLeg, TradeTemplateHelper trdTemplateHelper, MmTrade trdCtx) {

                    var currentDate = trdTemplateHelper.currentDateForTrdTemplate();
                    var maturityLeg = trdCtx.maturityLeg();
                    final LocalDate maturityLegValueDate;
                    // refer instance method: getRealOrPotentialMaturityLegValueDate(principalLeg, maturityLeg);
                    if (maturityLeg == null) {
                        maturityLegValueDate = principalLeg.valueDate().plusDays(trdTemplateHelper.currentDayForTrdTemplate() + 360);
                    } else {
                        maturityLegValueDate = maturityLeg.valueDate();
                    }
                    // New valueDate for principal leg
                    LocalDate newValueDate = trdTemplateHelper.getFutureValueDate(1, currentDate, maturityLegValueDate);
                    return new AmendableField.ValueDate(newValueDate);
                }
            }
        }
    }
}
