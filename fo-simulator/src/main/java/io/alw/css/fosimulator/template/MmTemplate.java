package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.*;
import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.model.Entity;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.common.CashflowIds;
import io.alw.css.fosimulator.template.common.InterestBasis;
import io.alw.css.fosimulator.template.common.InterestPayoutFrequency;
import io.alw.css.fosimulator.template.common.MmTemplateType;
import io.alw.datagen.provider.AbstractCyclicDataProvider;
import io.alw.datagen.template.TemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
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

public final class MmTemplate extends CashMessageTemplateWithDataStore {
    private final static Predicate<FoCashMessage> amendableMsgCriteria = ;
    private final Supplier<MmType> rndmMmTypeSelector = new CyclicMmTypeProvider(List.of(TERM, TERM, CALL));
    private final Supplier<RateType> rndmRateTypeSelector = new CyclicRateTypeProvider(List.of(FIXED, FLOAT, FIXED, FLOAT));
    private final Supplier<InterestPayoutFrequency> rndmIpFrequencySelector = new CyclicInterestPayoutFrequencyProvider(List.of(DAY, MONTHLY, MONTHLY, MONTHLY, PRINCIPAL_MATURITY, MONTHLY, QUARTERLY, QUARTERLY, SEMI_ANNUALLY, YEARLY, PRINCIPAL_MATURITY));

    public MmTemplate(Entity entity, TradeType tradeType, TransactionType transactionType, RandomGenerator rndm, LocalDate initialValueDate, RefDataService refDataService, DayTicker dayTicker, CashMessageTemplateProperties cashMsgTemplateProps) {
        super(entity, tradeType, transactionType, rndm, initialValueDate, refDataService, dayTicker, cashMsgTemplateProps);
    }

    @Override
    public List<FoCashMessage> get() {
        // Get cash messages that need to be amended
        final List<FoCashMessage> messagesToBeAmended = msgStoreHelper.getMessagesToBeAmended();

        // Build amended cashMessages and cashMessages for a new MM trade. An MM Trade can have three type of cashflows: Principal, Interest and Maturity
        List<FoCashMessage> newAndAmendedMsgs = ((MmTemplate) newTemplateBuilder())
                .withAmendedMessagesOf(messagesToBeAmended)
                .withTemplateValues()
                .buildWithRelatedTemplates();

        // Select new cash messages for future amendments and add to the message store
        msgStoreHelper.selectAmendCandidatesAndSave(newAndAmendedMsgs, amendableMsgCriteria);

        return newAndAmendedMsgs;
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
        Map<MmLeg, MmTemplateType> templateTypes = getNewMmTemplateTypes();
        // Build PRINCIPAL leg of the MoneyMarket trade irrespective of the MM trade type. (All MM trade types have a Principal leg)
        MmTemplateType principalLegTemplateType = templateTypes.get(PRINCIPAL);
        IdProvider idProvider = IdProvider.singleton();

        // Build PRINCIPAL leg of the MoneyMarket trade with base values
        FoCashMessageBuilder bdr = getNewFoCashMsgBuilder();
        // Set values specific to the PRINCIPAL leg
        bdr
                .valueDate(msgTemplateHelper.getRndmValueDate(30))
                .payOrReceive(rndm.nextBoolean() ? PAY : RECEIVE)
                .amount(BigDecimal.valueOf(rndm.nextDouble(10000, 98500000)))
        ;

        final CashflowIds maturityLegIds;
        final TradeLink maturityLegLink;
        final CashflowIds interestLegIds;
        final TradeLink interestLegLink;
        final MmTemplateType maturityTemplateType = templateTypes.get(MATURITY);
        final MmTemplateType interestTemplateType = templateTypes.get(INTEREST);

        if (maturityTemplateType != null && interestTemplateType != null) {
            maturityLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            maturityLegLink = buildTradeLink(MATURITY, maturityLegIds);
            interestLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            interestLegLink = buildTradeLink(INTEREST, interestLegIds);
        } else if (interestTemplateType != null) {
            interestLegIds = new CashflowIds(idProvider.nextCashflowId(), VERSION_ONE, bdr.tradeID(), bdr.tradeVersion());
            interestLegLink = buildTradeLink(INTEREST, interestLegIds);
            maturityLegIds = null;
            maturityLegLink = null;
        } else if (maturityTemplateType != null) {
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
        if (maturityTemplateType != null && interestTemplateType != null) {
            this.withRelatedTemplate((principalLeg) -> buildMaturityAndInterestLeg(principalLeg, maturityLegIds, interestLegIds, interestTemplateType, maturityTemplateType));
            bdr.tradeLinks(List.of(maturityLegLink, interestLegLink));
        } else if (interestTemplateType != null) {
            this.withRelatedTemplate((principalLeg) -> buildInterestLeg(principalLeg, null, interestLegIds, null, interestTemplateType));
            bdr.tradeLinks(List.of(interestLegLink));
        } else if (maturityTemplateType != null) {
            this.withRelatedTemplate((principalLeg) -> buildMaturityLeg(principalLeg, maturityLegIds, maturityTemplateType));
            bdr.tradeLinks(List.of(maturityLegLink));
        }

        return this;
    }

    private FoCashMessage buildMaturityAndInterestLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, CashflowIds interestLegIds, MmTemplateType interestTemplateType, MmTemplateType maturityTemplateType) {
        // Determine valueDate of MATURITY leg ahead of building the MATURITY leg as it is needed for creating interest leg. A callback can also be used, but it requires changes and new wrapping objects in TemplateBuilder class
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(principalLeg.valueDate(), false, 10);
        // Add building function of INTEREST leg
        this.withRelatedTemplate((principalLegParam) -> buildInterestLeg(principalLegParam, maturityLegIds, interestLegIds, maturityLegValueDate, interestTemplateType));
        // Build the MATURITY leg
        return buildMaturityLeg(principalLeg, maturityLegIds, interestLegIds, maturityLegValueDate, maturityTemplateType);
    }

    private FoCashMessage buildInterestLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, CashflowIds interestLegIds, LocalDate maturityLegValueDate, MmTemplateType interestTemplateType) {
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
                .valueDate(determineValueDateForInterestLeg(principalLeg.valueDate(), interestTemplateType, maturityLegValueDate))
                .payOrReceive(principalLeg.payOrReceive() == PAY ? RECEIVE : PAY)
                .amount(BigDecimal.valueOf(rndm.nextDouble(, ))) // TODO: Calculate a meaningful amount
                .tradeLinks(maturityLegTradeLink != null ? List.of(principalLegTradeLink, maturityLegTradeLink) : List.of(principalLegTradeLink));

        if (interestTemplateType.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateSelector.get());
        }

        return bdr.build();
    }

    private LocalDate determineValueDateForInterestLeg(LocalDate principalLegValueDate, MmTemplateType mmTemplateType, LocalDate maturityLegValueDate) {
        return switch (mmTemplateType.interestBasis()) {
            case ThirtyBy360 -> switch (mmTemplateType.ipFrequency()) {
                case DAY -> msgTemplateHelper.getFutureValueDate(1, principalLegValueDate, maturityLegValueDate);
                case MONTHLY -> msgTemplateHelper.getFutureValueDate(30, principalLegValueDate, maturityLegValueDate);
                case QUARTERLY -> msgTemplateHelper.getFutureValueDate(90, principalLegValueDate, maturityLegValueDate);
                case SEMI_ANNUALLY -> msgTemplateHelper.getFutureValueDate(180, principalLegValueDate, maturityLegValueDate);
                case YEARLY -> msgTemplateHelper.getFutureValueDate(360, principalLegValueDate, maturityLegValueDate);
                case PRINCIPAL_MATURITY -> maturityLegValueDate;
            };
        };
    }

    private FoCashMessage buildMaturityLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, MmTemplateType maturityTemplateType) {
        LocalDate maturityLegValueDate = msgTemplateHelper.getRndmFutureValueDateRelativeTo(principalLeg.valueDate(), false, 10);
        return buildMaturityLeg(principalLeg, maturityLegIds, null, maturityLegValueDate, maturityTemplateType);
    }

    private FoCashMessage buildMaturityLeg(FoCashMessage principalLeg, CashflowIds maturityLegIds, CashflowIds interestLegIds, LocalDate valueDate, MmTemplateType maturityTemplateType) {
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

        if (maturityTemplateType.rateType() == FLOAT && (VERSION_ONE != bdr.cashflowVersion() || VERSION_ONE != bdr.tradeVersion())) {
            bdr.rate(cyclicRateSelector.get());
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
    private Map<MmLeg, MmTemplateType> getNewMmTemplateTypes() {
        var mmType = rndmMmTypeSelector.get();
        var rateType = rndmRateTypeSelector.get();
        var ipFrequency = rndmIpFrequencySelector.get();
        var basis = InterestBasis.ThirtyBy360;

        return switch (mmType) {
            case TERM -> Map.of(
                    PRINCIPAL, new MmTemplateType.Term(PRINCIPAL, rateType, ipFrequency, basis),
                    MATURITY, new MmTemplateType.Call(MATURITY, rateType, ipFrequency, basis),
                    INTEREST, new MmTemplateType.Call(INTEREST, rateType, ipFrequency, basis));
            case CALL -> Map.of(
                    PRINCIPAL, new MmTemplateType.Term(PRINCIPAL, rateType, ipFrequency, basis),
                    INTEREST, new MmTemplateType.Call(INTEREST, rateType, ipFrequency, basis));
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
