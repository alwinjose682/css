package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.template.model.AmendableFoCashMessageField;
import io.alw.css.fosimulator.template.model.CashLegType;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.model.*;
import io.alw.datagen.provider.AbstractCyclicDataProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.cashflow.MmLeg.*;
import static io.alw.css.domain.cashflow.MmTradeType.CALL;
import static io.alw.css.domain.cashflow.MmTradeType.TERM;
import static io.alw.css.domain.cashflow.PayOrReceive.PAY;
import static io.alw.css.domain.cashflow.PayOrReceive.RECEIVE;
import static io.alw.css.domain.cashflow.RateType.FIXED;
import static io.alw.css.domain.cashflow.RateType.FLOAT;
import static io.alw.css.fosimulator.template.model.CashLegType.*;
import static io.alw.css.fosimulator.template.model.InterestPayoutFrequency.*;

public final class MmTemplate extends CashMessageTemplateWithDataStore<MmCashMessageContext> {
    // Message Store and Related
    private final CashMessageStoreHelper<MmCashMessageContext> msgStoreHelper;
    private final Predicate<MmCashMessageContext> amendableMsgSelectionCriteria = ;

    // Metadata for MM FoCashMessage
    private static final Supplier<MmTradeType> cyclicMmTypeProvider = new CyclicMmTypeProvider(List.of(TERM, TERM, CALL));
    private static final Supplier<RateType> cyclicRateTypeProvider = new CyclicRateTypeProvider(List.of(FIXED, FLOAT, FIXED, FLOAT));
    private static final Supplier<InterestPayoutFrequency> cyclicIpFrequencyProvider = new CyclicInterestPayoutFrequencyProvider(List.of(DAY, MONTHLY, MONTHLY, MONTHLY, PRINCIPAL_MATURITY, MONTHLY, QUARTERLY, QUARTERLY, SEMI_ANNUALLY, YEARLY, PRINCIPAL_MATURITY));
    private static final Supplier<Set<AmendableFoCashMessageField>> cyclicAmendableFoCashMessageFieldProvider = new CyclicAmendableFoCashMessageFieldProvider(getListOfAmendableCashMessageFields());
    private static final Supplier<CashLegType> cyclicAmendableMmLegProvider = new CyclicAmendableCashLegTypeProvider(List.of(MM_PRINCIPAL, MM_MATURITY, MM_MATURITY, MM_MATURITY, MM_MATURITY, MM_PRINCIPAL, MM_MATURITY));

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);

        CashMessageStore<MmCashMessageContext> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    /// Build new template for MM cashflow. A new MM trade can have 1 to 3 cashflows depending on whether its a TERM or CALL and depending on the interest cashflow
    @Override
    public MmTemplate withRootTemplateValues() {
        // Create MessageContext
        MmCashMessageContext msgCtx = createMessageContext();
        // Create Ids for PRINCIPAL, INTEREST and if applicable for MATURITY as well
        Map<MmLeg, Ids> idsMap = createFirstVersionIds(msgCtx);
        // Build PRINCIPAL leg of the MoneyMarket trade with base values
        FoCashMessageBuilder bdr = getNewCashMsgBuilder(idsMap.get(PRINCIPAL), msgCtx);
        // Set values specific to the PRINCIPAL leg
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(100000, 98500000)))
        ;

        // Set tradeLinks on the PRINCIPAL leg
        MmCashLeg maturityLeg = msgCtx.maturity();
        InterestCashLeg interestLeg = msgCtx.interests().getFirst();

        if (maturityLeg != null && interestLeg != null) {
            buildMaturityAndInterestLeg(msgCtx, idsMap.get(MATURITY), idsMap.get(INTEREST));
        } else if (interestLeg != null) {
            this.withGroupedItem(callback, () -> buildInterestLeg(msgCtx, idsMap.get(INTEREST), null));
        } else if (maturityLeg != null) {
            this.withGroupedItem(callback, () -> buildMaturityLeg(msgCtx, idsMap.get(MATURITY)));
        }

        return this;
    }

    @Override
    protected void buildAmendedMessage(Consumer<CashMessageAmendmentContext> buildAmendedMessageFunc, MmCashMessageContext msgCtxForAmendment) {
        var nextEventAndAction = getNextEventActionPair(msg.tradeEventType(), msg.tradeEventAction());
        var fieldsForAmendment = cyclicAmendableFoCashMessageFieldProvider.get();

        switch (cyclicAmendableMmLegProvider.get()) {
            case MM_PRINCIPAL -> {
                var primAndSecAmendSubCtxs = determineCashMsgsForAmendment(msgCtxForAmendment, fieldsForAmendment, MM_PRINCIPAL);
            }
            case MM_MATURITY -> {
            }
            case MM_INTEREST -> throw new RuntimeException("Amending interest leg is not allowed");
            default -> throw new RuntimeException("Invalid cash leg type for MM Trade");
        }


    }

    @Override
    protected PrimaryAndSecondaryAmendmentSubjectContexts determineCashMsgsForAmendment(MmCashMessageContext msgCtxForAmendment, Set<AmendableFoCashMessageField> fieldsForAmendment, CashLegType primaryAmendmentSubjectCashLegType) {

    }

    @Override
    protected TradeEventActionPair getNextEventActionPair(TradeEventType amendMsgEvt, TradeEventAction amendMsgAct) {

    }

    private void buildMaturityAndInterestLeg(MmCashMessageContext msgCtx, Ids maturityLegIds, Ids interestLegIds) {
        // Determine valueDate of MATURITY leg ahead of building the MATURITY leg as it is needed for creating interest leg. A callback can also be used, but it requires changes and new wrapping objects in TemplateBuilder class
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(msgCtx.rootFoCashMessage().valueDate(), false, 10);
        // Add building function of MATURITY leg
        this.withGroupedItem(callback, () -> buildMaturityLeg(msgCtx, maturityLegIds, maturityLegValueDate));
        // Add building function of INTEREST leg
        this.withGroupedItem(callback, () -> buildInterestLeg(msgCtx, interestLegIds, maturityLegValueDate));
    }

    private FoCashMessageBuilder buildInterestLeg(MmCashMessageContext msgCtx, Ids interestLegIds, LocalDate maturityLegValueDate) {
        var principalLeg = msgCtx.rootFoCashMessage();
        InterestCashLeg interestCashLeg = msgCtx.interests().getFirst();

        if (maturityLegValueDate == null) {
            maturityLegValueDate = principalLeg.valueDate().plusDays(msgTemplateHelper.dayForMsgTemplate() + 360);
        }

        // Build the INTEREST leg
        var bdr = createBuilderFrom(principalLeg, MM_INTEREST)
                // Id and version of INTEREST leg was already determined when the PRINCIPAL was created
                .cashflowID(interestLegIds.cashflowID())
                .cashflowVersion(interestLegIds.cashflowVersion())
                .tradeID(interestLegIds.tradeID())
                .tradeVersion(interestLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(principalLeg.valueDate(), interestCashLeg, maturityLegValueDate))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(determineInterestLegAmount(principalLeg, maturityLegValueDate, interestCashLeg));

        if (interestCashLeg.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    /// NOTE: The amount returned is not a result of a proper calculation based on rate.
    /// It is just a meaningful enough number for an interest leg when the following parameters are taken into consideration:
    /// - principalAmount, interest basis, interest payout frequency, rate type and maturity date
    ///
    /// This is done solely to avoid calculations using BigDecimal. Actual amount is not required.
    private BigDecimal determineInterestLegAmount(FoCashMessage principalLeg, LocalDate maturityLegValueDate, InterestCashLeg interestCashLeg) {

        // Re-use the interest amount if it was already determined, but only if the values used to determine has not changed
        var intrLegCtx = interestCashLeg.interestLegContext();
        if (intrLegCtx != null) {
            var lastUsedInterestAmount = intrLegCtx.lastUsedInterestAmount();
            var lastUsedPrincipalAmount = intrLegCtx.lastUsedPrincipalAmount();
            var lastUsedPrincipalValueDate = intrLegCtx.lastUsedPrincipalValueDate();
            var lastUsedMaturityValueDate = intrLegCtx.lastUsedMaturityValueDate();

            if (lastUsedPrincipalAmount.equals(principalLeg.amount())
                    && lastUsedPrincipalValueDate.equals(principalLeg.valueDate())
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
        var principalAmount = principalLeg.amount();
        var principalValueDate = principalLeg.valueDate();
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

    private LocalDate determineInterestLegValueDate(LocalDate principalLegValueDate, MmMetadata interestMetadata, LocalDate maturityLegValueDate) {
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

    private FoCashMessageBuilder buildMaturityLeg(MmCashMessageContext msgCtx, Ids maturityLegIds) {
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(msgCtx.rootFoCashMessage().valueDate(), false, 10);
        return buildMaturityLeg(msgCtx, maturityLegIds, maturityLegValueDate);
    }

    private FoCashMessageBuilder buildMaturityLeg(MmCashMessageContext msgCtx, Ids maturityLegIds, LocalDate maturityLegValueDate) {
        var principalMsg = msgCtx.rootFoCashMessage();

        // Build the MATURITY leg
        var bdr = createBuilderFrom(principalMsg, MM_MATURITY)
                // Id and version of MATURITY leg was already determined when the PRINCIPAL was created
                .cashflowID(maturityLegIds.cashflowID())
                .cashflowVersion(maturityLegIds.cashflowVersion())
                .tradeID(maturityLegIds.tradeID())
                .tradeVersion(maturityLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(maturityLegValueDate)
                .payOrReceive(principalMsg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(principalMsg.amount().negate());

        if (msgCtx.principal().rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr;
    }

    private Map<MmLeg, Ids> createFirstVersionIds(MmCashMessageContext msgCtx) {
        var principalLegIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowAndVersionOneTrade(MM_PRINCIPAL);
        var interestLegIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowFromExistingTrade(MM_INTEREST, principalLegIds);

        if (msgCtx.principal().mmType() != CALL) {
            var maturityLegIds = CashMessageTemplateHelper.getIdsForVersionOneCashflowFromExistingTrade(MM_MATURITY, principalLegIds);
            return Map.of(PRINCIPAL, principalLegIds, INTEREST, interestLegIds, MATURITY, maturityLegIds);
        } else {
            return Map.of(PRINCIPAL, principalLegIds, INTEREST, interestLegIds);
        }
    }

    /// Interest Basis is always assigned a constant: InterestBasis.ThirtyBy360. Corresponding calculation for other basis types are not implemented.
    /// Interest cashflow should be generated based on [InterestPayoutFrequency]
    /// Returns message contexts for cashflow version 1. The method parameter `principalLegBuilder` must be of cashflow version 1
    private MmCashMessageContext createMessageContext() {
        var mmType = cyclicMmTypeProvider.get();
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = InterestBasis.ThirtyBy360;

        return switch (mmType) {
            case TERM -> {
                var principal = new MmCashLeg(TERM, MM_PRINCIPAL, rateType, ipFrequency, basis);
                var maturity = new MmCashLeg(TERM, MM_MATURITY, rateType, ipFrequency, basis);
                List<InterestCashLeg> interests = new ArrayList<>();
                interests.add(new InterestCashLeg(TERM, MM_INTEREST, rateType, ipFrequency, basis));

                yield new MmCashMessageContext(principal, interests, maturity);
            }
            case CALL -> {
                var principal = new MmCashLeg(CALL, MM_PRINCIPAL, rateType, ipFrequency, basis);
                List<InterestCashLeg> interests = new ArrayList<>();
                interests.add(new InterestCashLeg(CALL, MM_INTEREST, rateType, ipFrequency, basis));

                yield new MmCashMessageContext(principal, interests);
            }
        };
    }

    private static final class CyclicMmTypeProvider extends AbstractCyclicDataProvider<MmTradeType> {
        CyclicMmTypeProvider(List<MmTradeType> dataList) {
            super(dataList);
        }
    }

    private static final class CyclicRateTypeProvider extends AbstractCyclicDataProvider<RateType> {
        CyclicRateTypeProvider(List<RateType> dataList) {
            super(dataList);
        }
    }

    private static final class CyclicInterestPayoutFrequencyProvider extends AbstractCyclicDataProvider<InterestPayoutFrequency> {
        CyclicInterestPayoutFrequencyProvider(List<InterestPayoutFrequency> dataList) {
            super(dataList);
        }
    }

    @Override
    protected CashMessageStoreHelper<MmCashMessageContext> msgStoreHelper() {
        return msgStoreHelper;
    }

    @Override
    protected Predicate<MmCashMessageContext> amendableMsgSelectionCriteria() {
        return amendableMsgSelectionCriteria;
    }

    private static List<Set<AmendableFoCashMessageField>> getListOfAmendableCashMessageFields() {
        return List.of(
                Set.of(AmendableFoCashMessageField.CounterpartyCode.withZeroValue()),
                Set.of(AmendableFoCashMessageField.Amount.withZeroValue()),
                Set.of(AmendableFoCashMessageField.ValueDate.withZeroValue(),
                        AmendableFoCashMessageField.Amount.withZeroValue()),
                Set.of(AmendableFoCashMessageField.CounterpartyCode.withZeroValue(),
                        AmendableFoCashMessageField.Amount.withZeroValue()),
                Set.of(AmendableFoCashMessageField.ValueDate.withZeroValue(),
                        AmendableFoCashMessageField.Amount.withZeroValue(),
                        AmendableFoCashMessageField.CounterpartyCode.withZeroValue())

        );
    }

    private static class CyclicAmendableFoCashMessageFieldProvider extends AbstractCyclicDataProvider<Set<AmendableFoCashMessageField>> {
        public CyclicAmendableFoCashMessageFieldProvider(List<Set<AmendableFoCashMessageField>> fields) {
            super(fields);
        }
    }

    private static class CyclicAmendableCashLegTypeProvider extends AbstractCyclicDataProvider<CashLegType> {
        public CyclicAmendableCashLegTypeProvider(List<CashLegType> mmLegs) {
            super(mmLegs);
        }
    }
}
