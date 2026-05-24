package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.store.CashMessageStore;
import io.alw.css.fosimulator.store.InMemoryCashMessageStore;
import io.alw.css.fosimulator.template.common.*;
import io.alw.datagen.provider.AbstractCyclicDataProvider;
import io.alw.datagen.template.TemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.cashflow.MmLeg.*;
import static io.alw.css.domain.cashflow.MmType.CALL;
import static io.alw.css.domain.cashflow.MmType.TERM;
import static io.alw.css.domain.cashflow.PayOrReceive.PAY;
import static io.alw.css.domain.cashflow.PayOrReceive.RECEIVE;
import static io.alw.css.domain.cashflow.RateType.FIXED;
import static io.alw.css.domain.cashflow.RateType.FLOAT;
import static io.alw.css.fosimulator.template.common.InterestPayoutFrequency.*;

public final class MmTemplate extends CashMessageTemplateWithDataStore<MmCashMessageContext> {
    private final Supplier<MmType> cyclicMmTypeProvider = new CyclicMmTypeProvider(List.of(TERM, TERM, CALL));
    private final Supplier<RateType> cyclicRateTypeProvider = new CyclicRateTypeProvider(List.of(FIXED, FLOAT, FIXED, FLOAT));
    private final Supplier<InterestPayoutFrequency> cyclicIpFrequencyProvider = new CyclicInterestPayoutFrequencyProvider(List.of(DAY, MONTHLY, MONTHLY, MONTHLY, PRINCIPAL_MATURITY, MONTHLY, QUARTERLY, QUARTERLY, SEMI_ANNUALLY, YEARLY, PRINCIPAL_MATURITY));

    // Message Store and Related
    private final CashMessageStoreHelper<MmCashMessageContext> msgStoreHelper;
    private final Predicate<FoCashMessage> amendableMsgSelectionCriteria = ;

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);

        CashMessageStore<MmCashMessageContext> msgStore = new InMemoryCashMessageStore<>();
        this.msgStoreHelper = new CashMessageStoreHelper<>(dayTicker, msgStore, rndm, msgTemplateHelper);
    }

    @Override
    protected CashMessageTemplateWithDataStore<MmCashMessageContext> templateBuildSteps() {
        // Build cash messages for a new MM trade(new+amendments). An MM Trade can have three type of cashflows: Principal, Interest and Maturity
        ((MmTemplate) newTemplateBuilder())
                .withMessageAmendments()
                .withTemplateValues();

        return this;
    }

    @Override
    protected List<MmCashMessageContext> mapToMessageContext(List<FoCashMessage> cashMessages) {

    }

    @Override
    protected List<FoCashMessage> mapToCashMessage(List<MmCashMessageContext> messageContext) {

    }

    @Override
    protected CashMessageStoreHelper<MmCashMessageContext> msgStoreHelper() {
        return msgStoreHelper;
    }

    @Override
    protected Predicate<FoCashMessage> amendableMsgSelectionCriteria() {
        return amendableMsgSelectionCriteria;
    }

    @Override
    protected TradeEventActionPair getNextEventActionPair(TradeEventType amendMsgEvt, TradeEventAction amendMsgAct) {

    }

    /// Build new template for MM cashflow. A new MM trade can have 1 to 3 cashflows depending on whether its a TERM or CALL and depending on the interest cashflow
    @Override
    public TemplateBuilder<FoCashMessage> withTemplateValues() {
        // Build PRINCIPAL leg of the MoneyMarket trade with base values
        FoCashMessageBuilder bdr = getNewFoCashMsgBuilder();
        // Create CashMessageContext for all three legs: PRINCIPAL, MATURITY, INTEREST
        Map<MmLeg, MmCashMessageContext> msgContexts = getFirstVersionMsgContexts(bdr);
        // Set values specific to the PRINCIPAL leg
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(100000, 98500000)))
        ;

        final var principalContext = msgContexts.get(PRINCIPAL);
        final var maturityContext = msgContexts.get(MATURITY);
        final var interestContext = msgContexts.get(INTEREST);

        // Set tradeLinks on the PRINCIPAL leg
        if (maturityContext != null && interestContext != null) {
            this.withRelatedTemplate((principalLeg) -> buildMaturityAndInterestLeg(principalLeg, interestContext, maturityContext));
            bdr.tradeLinks(principalContext.allTradeLinks());
        } else if (interestContext != null) {
            this.withRelatedTemplate((principalLeg) -> buildInterestLeg(principalLeg, null, interestContext));
            bdr.tradeLinks(principalContext.allTradeLinks());
        } else if (maturityContext != null) {
            this.withRelatedTemplate((principalLeg) -> buildMaturityLeg(principalLeg, maturityContext));
            bdr.tradeLinks(principalContext.allTradeLinks());
        }

        return this;
    }

    private FoCashMessage buildMaturityAndInterestLeg(FoCashMessage principalLeg, MmCashMessageContext interestContext, MmCashMessageContext maturityContext) {
        // Determine valueDate of MATURITY leg ahead of building the MATURITY leg as it is needed for creating interest leg. A callback can also be used, but it requires changes and new wrapping objects in TemplateBuilder class
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(principalLeg.valueDate(), false, 10);
        // Add building function of INTEREST leg
        this.withRelatedTemplate((principalLegParam) -> buildInterestLeg(principalLegParam, maturityLegValueDate, interestContext));
        // Build the MATURITY leg
        return buildMaturityLeg(principalLeg, interestContext.cashflowIds(), maturityLegValueDate, maturityContext);
    }

    private FoCashMessage buildInterestLeg(FoCashMessage principalLeg, LocalDate maturityLegValueDate, MmCashMessageContext interestContext) {
        // build the INTEREST leg
        var interestLegIds = interestContext.cashflowIds();
        var bdr = createBuilderFrom(principalLeg)
                // Id and version of INTEREST leg was already determined when the PRINCIPAL was created
                .cashflowID(interestLegIds.cashflowID())
                .cashflowVersion(interestLegIds.cashflowVersion())
                .tradeID(interestLegIds.tradeID())
                .tradeVersion(interestLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(principalLeg.valueDate(), interestContext, maturityLegValueDate))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(determineInterestLegAmount(principalLeg, maturityLegValueDate, interestContext))
                .tradeLinks(interestContext.allTradeLinks());

        if (interestContext.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr.build();
    }

    /// NOTE: The amount returned is not a result of a proper calculation based on rate.
    /// It is just a meaningful enough number for an interest leg when the following parameters are taken into consideration:
    /// - principalAmount, interest basis, interest payout frequency, rate type and maturity date
    ///
    /// This is done solely to avoid calculations using BigDecimal. Actual amount is not required.
    private BigDecimal determineInterestLegAmount(FoCashMessage principalLeg, LocalDate maturityLegValueDate, MmCashMessageContext interestContext) {

        // If the interest amount was already determined, just re-use it
        BigDecimal interestAmount = interestContext.interestAmount();
        if (interestAmount != null) {
            return switch (interestContext.rateType()) {
                case FIXED -> interestAmount;
                case FLOAT -> {
                    BigDecimal newIntAmt = BigDecimal.valueOf(interestAmount.doubleValue() + 158);// Even though rate decrease the amount increases. This is ok! Actual amount is not required
                    interestContext.setInterestAmount(newIntAmt);
                    yield newIntAmt;
                }
            };
        }

        // Determine the interest amount
        var principalAmount = principalLeg.amount();
        var principalValueDate = principalLeg.valueDate();
        var newInterestAmount = switch (interestContext.interestBasis()) {
            case ThirtyBy360 -> {
                long numOfDays = ChronoUnit.DAYS.between(principalValueDate, maturityLegValueDate);
                double tenPercentOfPrincipal = (principalAmount.doubleValue() / 100) * 10;

                yield switch (interestContext.ipFrequency()) {
                    case DAY -> BigDecimal.valueOf(tenPercentOfPrincipal / (double) numOfDays);
                    case MONTHLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 30));
                    case QUARTERLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 90));
                    case SEMI_ANNUALLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 180));
                    case YEARLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 360));
                    case PRINCIPAL_MATURITY -> BigDecimal.valueOf(tenPercentOfPrincipal);
                };
            }
        };

        interestContext.setInterestAmount(newInterestAmount);
        return newInterestAmount;
    }

    private LocalDate determineInterestLegValueDate(LocalDate principalLegValueDate, MmCashMessageContext interestContext, LocalDate maturityLegValueDate) {
        return switch (interestContext.interestBasis()) {
            case ThirtyBy360 -> switch (interestContext.ipFrequency()) {
                case DAY -> msgTemplateHelper.getFutureValueDate(1, principalLegValueDate, maturityLegValueDate);
                case MONTHLY -> msgTemplateHelper.getFutureValueDate(30, principalLegValueDate, maturityLegValueDate);
                case QUARTERLY -> msgTemplateHelper.getFutureValueDate(90, principalLegValueDate, maturityLegValueDate);
                case SEMI_ANNUALLY -> msgTemplateHelper.getFutureValueDate(180, principalLegValueDate, maturityLegValueDate);
                case YEARLY -> msgTemplateHelper.getFutureValueDate(360, principalLegValueDate, maturityLegValueDate);
                case PRINCIPAL_MATURITY -> maturityLegValueDate;
            };
        };
    }

    private FoCashMessage buildMaturityLeg(FoCashMessage principalLeg, MmCashMessageContext maturityContext) {
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(principalLeg.valueDate(), false, 10);
        return buildMaturityLeg(principalLeg, null, maturityLegValueDate, maturityContext);
    }

    private FoCashMessage buildMaturityLeg(FoCashMessage principalLeg, CashflowIds interestLegIds, LocalDate valueDate, MmCashMessageContext maturityContext) {
        // Build the MATURITY leg
        var maturityLegIds = maturityContext.cashflowIds();
        var bdr = createBuilderFrom(principalLeg)
                // Id and version of MATURITY leg was already determined when the PRINCIPAL was created
                .cashflowID(maturityLegIds.cashflowID())
                .cashflowVersion(maturityLegIds.cashflowVersion())
                .tradeID(maturityLegIds.tradeID())
                .tradeVersion(maturityLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(valueDate)
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(principalLeg.amount().negate())
                .tradeLinks(maturityContext.allTradeLinks());

        if (maturityContext.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr.build();
    }

    private TradeLink buildTradeLink(MmLeg mmLeg, CashflowIds cashflowIds) {
        return TradeLinkBuilder.builder()
                .linkType(mmLeg.name())
                .relatedFoCashflowID(cashflowIds.cashflowID())
                .relatedFoCashflowVersion(cashflowIds.cashflowVersion())
                .relatedTradeID(cashflowIds.tradeID())
                .relatedTradeVersion(cashflowIds.tradeVersion())
                .build();
    }

    /// Interest Basis is always assigned a constant: InterestBasis.ThirtyBy360. Corresponding calculation for other basis types are not implemented.
    /// Interest cashflow should be generated based on [InterestPayoutFrequency]
    /// Returns message contexts for cashflow version 1. The method parameter `principalLegBuilder` must be of cashflow version 1
    private Map<MmLeg, MmCashMessageContext> getFirstVersionMsgContexts(FoCashMessageBuilder principalLegBuilder) {
        var mmType = cyclicMmTypeProvider.get();
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = InterestBasis.ThirtyBy360;

        var idProvider = IdProvider.singleton();

        var principalLegIds = new CashflowIds(principalLegBuilder.cashflowID(), principalLegBuilder.cashflowVersion(), principalLegBuilder.tradeID(), principalLegBuilder.tradeVersion());
        var principalLegLink = buildTradeLink(PRINCIPAL, principalLegIds);
        var interestLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, principalLegBuilder.tradeID(), principalLegBuilder.tradeVersion());
        var interestLegLink = buildTradeLink(INTEREST, interestLegIds);

        return switch (mmType) {
            case TERM -> {
                var maturityLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, principalLegBuilder.tradeID(), principalLegBuilder.tradeVersion());
                var maturityLegLink = buildTradeLink(MATURITY, maturityLegIds);
                var allTradeLinks = List.of(principalLegLink, interestLegLink, maturityLegLink);

                yield Map.of(
                        PRINCIPAL, new MmCashMessageContext(TERM, PRINCIPAL, rateType, ipFrequency, basis, principalLegIds, allTradeLinks),
                        MATURITY, new MmCashMessageContext(TERM, MATURITY, rateType, ipFrequency, basis, maturityLegIds, allTradeLinks),
                        INTEREST, new MmCashMessageContext(TERM, INTEREST, rateType, ipFrequency, basis, interestLegIds, allTradeLinks));
            }
            case CALL -> {
                var allTradeLinks = List.of(principalLegLink, interestLegLink);
                yield Map.of(
                        PRINCIPAL, new MmCashMessageContext(CALL, PRINCIPAL, rateType, ipFrequency, basis, principalLegIds, allTradeLinks),
                        INTEREST, new MmCashMessageContext(CALL, INTEREST, rateType, ipFrequency, basis, interestLegIds, allTradeLinks));
            }
        };
    }

    private static final class CyclicMmTypeProvider extends AbstractCyclicDataProvider<MmType> {
        CyclicMmTypeProvider(List<MmType> dataList) {
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
}
