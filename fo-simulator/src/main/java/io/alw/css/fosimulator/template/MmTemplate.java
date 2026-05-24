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
    /// I have decided how to move forward -> keep a Map that stores the metadata. The unique metadata corresponding to an FoCashMessage can be retrieved using the key: FoCashMessage.cashflowID-cashflowVersion-tradeID-tradeVersion
    /// There is no need to modify the message store to store a generic type.
    @Override
    public TemplateBuilder<FoCashMessage> withTemplateValues() {
        // Select the type of MM trade
        Map<MmLeg, MmTemplateMetadata> templateTypes = getNewMmTemplateTypes();
        // Build PRINCIPAL leg of the MoneyMarket trade irrespective of the MM trade type. (All MM trade types have a Principal leg)
        MmTemplateMetadata principalLegTemplateType = templateTypes.get(PRINCIPAL);
        IdProvider idProvider = IdProvider.singleton();

        // Build PRINCIPAL leg of the MoneyMarket trade with base values
        FoCashMessageBuilder bdr = getNewFoCashMsgBuilder();
        // Set values specific to the PRINCIPAL leg
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(100000, 98500000)))
        ;

        final CashflowIds maturityLegIds;
        final TradeLink maturityLegLink;
        final CashflowIds interestLegIds;
        final TradeLink interestLegLink;
        final MmTemplateMetadata maturityTemplateMetadata = templateTypes.get(MATURITY);
        final MmTemplateMetadata interestTemplateMetadata = templateTypes.get(INTEREST);

        if (maturityTemplateMetadata != null && interestTemplateMetadata != null) {
            maturityLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            maturityLegLink = buildTradeLink(MATURITY, maturityLegIds);
            interestLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            interestLegLink = buildTradeLink(INTEREST, interestLegIds);
        } else if (interestTemplateMetadata != null) {
            interestLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            interestLegLink = buildTradeLink(INTEREST, interestLegIds);
            maturityLegIds = null;
            maturityLegLink = null;
        } else if (maturityTemplateMetadata != null) {
            maturityLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            maturityLegLink = buildTradeLink(MATURITY, maturityLegIds);
            interestLegIds = null;
            interestLegLink = null;
        } else {
            maturityLegIds = null;
            maturityLegLink = null;
            interestLegIds = null;
            interestLegLink = null;
        }

        // Set tradeLinks on the PRINCIPAL leg
        if (maturityTemplateMetadata != null && interestTemplateMetadata != null) {
            this.withRelatedTemplate((principalLeg) -> buildMaturityAndInterestLeg(principalLeg, maturityLegIds, interestLegIds, interestTemplateMetadata, maturityTemplateMetadata));
            bdr.tradeLinks(List.of(maturityLegLink, interestLegLink));
        } else if (interestTemplateMetadata != null) {
            this.withRelatedTemplate((principalLeg) -> buildInterestLeg(principalLeg, null, interestLegIds, null, interestTemplateMetadata));
            bdr.tradeLinks(List.of(interestLegLink));
        } else if (maturityTemplateMetadata != null) {
            this.withRelatedTemplate((principalLeg) -> buildMaturityLeg(principalLeg, maturityLegIds, maturityTemplateMetadata));
            bdr.tradeLinks(List.of(maturityLegLink));
        }

        return this;
    }

    private FoCashMessage buildMaturityAndInterestLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, CashflowIds interestLegIds, MmTemplateMetadata interestTemplateMetadata, MmTemplateMetadata maturityTemplateMetadata) {
        // Determine valueDate of MATURITY leg ahead of building the MATURITY leg as it is needed for creating interest leg. A callback can also be used, but it requires changes and new wrapping objects in TemplateBuilder class
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(principalLeg.valueDate(), false, 10);
        // Add building function of INTEREST leg
        this.withRelatedTemplate((principalLegParam) -> buildInterestLeg(principalLegParam, maturityLegIds, interestLegIds, maturityLegValueDate, interestTemplateMetadata));
        // Build the MATURITY leg
        return buildMaturityLeg(principalLeg, maturityLegIds, interestLegIds, maturityLegValueDate, maturityTemplateMetadata);
    }

    private FoCashMessage buildInterestLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, CashflowIds interestLegIds, LocalDate maturityLegValueDate, MmTemplateMetadata interestTemplateMetadata) {
        // Build tradeLinks for PRINCIPAL and MATURITY legs
        TradeLink principalLegTradeLink = buildTradeLink(PRINCIPAL, new CashflowIds(principalLeg.cashflowID(), principalLeg.cashflowVersion(), principalLeg.tradeID(), principalLeg.tradeVersion()));
        TradeLink maturityLegTradeLink = null;
        if (maturityLegIds != null) {
            maturityLegTradeLink = buildTradeLink(MATURITY, maturityLegIds);
        }

        // build the INTEREST leg
        var bdr = createBuilderFrom(principalLeg)
                // Id and version of INTEREST leg was already determined when the PRINCIPAL was created
                .cashflowID(interestLegIds.cashflowID())
                .cashflowVersion(interestLegIds.cashflowVersion())
                .tradeID(interestLegIds.tradeID())
                .tradeVersion(interestLegIds.tradeVersion())
                // Values that differ from PRINCIPAL leg
                .valueDate(determineInterestLegValueDate(principalLeg.valueDate(), interestTemplateMetadata, maturityLegValueDate))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(determineInterestLegAmount(principalLeg, maturityLegValueDate, interestTemplateMetadata))
                .tradeLinks(maturityLegTradeLink != null ? List.of(principalLegTradeLink, maturityLegTradeLink) : List.of(principalLegTradeLink));

        if (interestTemplateMetadata.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateProvider.get());
        }

        return bdr.build();
    }

    /// NOTE: The amount returned is not a result of a proper calculation based on rate.
    /// It is just a meaningful enough number for an interest leg when the following parameters are taken into consideration:
    /// - principalAmount, interest basis, interest payout frequency, rate type and maturity date
    ///
    /// This is done solely to avoid calculations using BigDecimal. Actual amount is not required.
    private BigDecimal determineInterestLegAmount(FoCashMessage principalLeg, LocalDate maturityLegValueDate, MmTemplateMetadata templateMetadata) {

        // If the interest amount was already determined, just re-use it
        BigDecimal interestAmount = templateMetadata.metadata().interestAmount();
        if (interestAmount != null) {
            return switch (templateMetadata.rateType()) {
                case FIXED -> interestAmount;
                case FLOAT -> {
                    BigDecimal newIntAmt = BigDecimal.valueOf(interestAmount.doubleValue() + 158);// Even though rate decrease the amount increases. This is ok! Actual amount is not required
                    templateMetadata.metadata().setInterestAmount(newIntAmt);
                    yield newIntAmt;
                }
            };
        }

        // Determine the interest amount
        var principalAmount = principalLeg.amount();
        var principalValueDate = principalLeg.valueDate();
        var newInterestAmount = switch (templateMetadata.interestBasis()) {
            case ThirtyBy360 -> {
                long numOfDays = ChronoUnit.DAYS.between(principalValueDate, maturityLegValueDate);
                double tenPercentOfPrincipal = (principalAmount.doubleValue() / 100) * 10;

                yield switch (templateMetadata.ipFrequency()) {
                    case DAY -> BigDecimal.valueOf(tenPercentOfPrincipal / (double) numOfDays);
                    case MONTHLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 30));
                    case QUARTERLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 90));
                    case SEMI_ANNUALLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 180));
                    case YEARLY -> BigDecimal.valueOf(tenPercentOfPrincipal / ((double) numOfDays / 360));
                    case PRINCIPAL_MATURITY -> BigDecimal.valueOf(tenPercentOfPrincipal);
                };
            }
        };

        templateMetadata.metadata().setInterestAmount(newInterestAmount);
        return newInterestAmount;
    }

    private LocalDate determineInterestLegValueDate(LocalDate principalLegValueDate, MmTemplateMetadata templateMetadata, LocalDate maturityLegValueDate) {
        return switch (templateMetadata.interestBasis()) {
            case ThirtyBy360 -> switch (templateMetadata.ipFrequency()) {
                case DAY -> msgTemplateHelper.getFutureValueDate(1, principalLegValueDate, maturityLegValueDate);
                case MONTHLY -> msgTemplateHelper.getFutureValueDate(30, principalLegValueDate, maturityLegValueDate);
                case QUARTERLY -> msgTemplateHelper.getFutureValueDate(90, principalLegValueDate, maturityLegValueDate);
                case SEMI_ANNUALLY -> msgTemplateHelper.getFutureValueDate(180, principalLegValueDate, maturityLegValueDate);
                case YEARLY -> msgTemplateHelper.getFutureValueDate(360, principalLegValueDate, maturityLegValueDate);
                case PRINCIPAL_MATURITY -> maturityLegValueDate;
            };
        };
    }

    private FoCashMessage buildMaturityLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, MmTemplateMetadata maturityTemplateMetadata) {
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(principalLeg.valueDate(), false, 10);
        return buildMaturityLeg(principalLeg, maturityLegIds, null, maturityLegValueDate, maturityTemplateMetadata);
    }

    private FoCashMessage buildMaturityLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, CashflowIds interestLegIds, LocalDate valueDate, MmTemplateMetadata maturityTemplateMetadata) {
        // Build tradeLinks for PRINCIPAL and INTEREST legs
        TradeLink principalLegTradeLink = buildTradeLink(PRINCIPAL, new CashflowIds(principalLeg.cashflowID(), principalLeg.cashflowVersion(), principalLeg.tradeID(), principalLeg.tradeVersion()));
        TradeLink interestLegTradeLink = null;
        if (interestLegIds != null) {
            interestLegTradeLink = buildTradeLink(INTEREST, interestLegIds);
        }

        // build the MATURITY leg
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
                .tradeLinks(interestLegTradeLink != null ? List.of(principalLegTradeLink, interestLegTradeLink) : List.of(principalLegTradeLink));

        if (maturityTemplateMetadata.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
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
    private Map<MmLeg, MmTemplateMetadata> getNewMmTemplateTypes() {
        var mmType = cyclicMmTypeProvider.get();
        var rateType = cyclicRateTypeProvider.get();
        var ipFrequency = cyclicIpFrequencyProvider.get();
        var basis = InterestBasis.ThirtyBy360;

        return switch (mmType) {
            case TERM -> Map.of(
                    PRINCIPAL, new MmTemplateMetadata.Term(PRINCIPAL, rateType, ipFrequency, basis),
                    MATURITY, new MmTemplateMetadata.Call(MATURITY, rateType, ipFrequency, basis),
                    INTEREST, new MmTemplateMetadata.Call(INTEREST, rateType, ipFrequency, basis));
            case CALL -> Map.of(
                    PRINCIPAL, new MmTemplateMetadata.Term(PRINCIPAL, rateType, ipFrequency, basis),
                    INTEREST, new MmTemplateMetadata.Call(INTEREST, rateType, ipFrequency, basis));
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
