package io.alw.css.tradeconsumer.confirmation.service;

import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.SentOrRecd;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.serialization.confirmation.TradeLegMatchAttributeAvro;
import io.alw.css.tradeconsumer.confirmation.model.ConfirmationMatchRequestFactoryOutcome;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;
import io.alw.css.tradeconsumer.model.CashflowSet;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.*;

public class TradeConfirmationServiceDelegate {

    static List<List<CashflowBuilder>> groupForMmTerm(List<CashflowBuilder> cashflowsBdrs) {
        Map<TradeLegType, String> primaryTradeLegTypes = new EnumMap<>(TradeLegType.class);

        // Determine the TradeLegTypes to add in the primaryTradeLegTypes collection,
        // because individual TradeLegs may also be generated according to the payment schedule(Ex: interest leg of MM trade, see TradePublisher's MmTemplate class)
        boolean hasPrincipalLeg = cashflowsBdrs.stream().anyMatch(cb -> cb.tradeLegType() == TradeLegType.MM_PRINCIPAL);
        if (hasPrincipalLeg) {
            primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
            primaryTradeLegTypes.put(TradeLegType.MM_MATURITY, "v");
        } else {
            primaryTradeLegTypes.put(TradeLegType.MM_INTEREST, "v");
        }

        return groupGenericMm(cashflowsBdrs, primaryTradeLegTypes);
    }

    static List<List<CashflowBuilder>> groupForMmCall(List<CashflowBuilder> cashflowsBdrs) {
        Map<TradeLegType, String> primaryTradeLegTypes = new EnumMap<>(TradeLegType.class);

        // Determine the TradeLegTypes to add in the primaryTradeLegTypes collection,
        // because individual TradeLegs may also be generated according to the payment schedule(Ex: interest leg of MM trade, see TradePublisher's MmTemplate class)
        boolean hasPrincipalLeg = cashflowsBdrs.stream().anyMatch(cb -> cb.tradeLegType() == TradeLegType.MM_PRINCIPAL);
        if (hasPrincipalLeg) {
            boolean hasMaturityLeg = cashflowsBdrs.stream().anyMatch(cb -> cb.tradeLegType() == TradeLegType.MM_MATURITY);
            if (hasMaturityLeg) {
                primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
                primaryTradeLegTypes.put(TradeLegType.MM_MATURITY, "v");
            } else {
                primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
            }
        } else {
            primaryTradeLegTypes.put(TradeLegType.MM_INTEREST, "v");
        }

        return groupGenericMm(cashflowsBdrs, primaryTradeLegTypes);
    }

    private static List<List<CashflowBuilder>> groupGenericMm(List<CashflowBuilder> cashflowsBdrs, Map<TradeLegType, String> primaryTradeLegTypes) {
        List<CashflowBuilder> remainingCashflows = new ArrayList<>();
        List<List<CashflowBuilder>> groupedCashflows = new ArrayList<>();

        // For Principal and Maturity legs
        // OR
        // For adhoc interest legs generated based on the payment schedule(see TradePublisher's MmTemplate class)
        List<CashflowBuilder> groupedPrimary = groupCashflowsForConfirmationMatchRequest(cashflowsBdrs, primaryTradeLegTypes, remainingCashflows);
        groupedCashflows.add(groupedPrimary);

        // For Interest legs
        while (!remainingCashflows.isEmpty()) {
            Map<TradeLegType, String> interestTradeLegTypes = new EnumMap<>(TradeLegType.class);
            interestTradeLegTypes.put(TradeLegType.MM_INTEREST, "v");
            var actionableCashflows = remainingCashflows;
            remainingCashflows = new ArrayList<>();

            List<CashflowBuilder> groupedSecondary = groupCashflowsForConfirmationMatchRequest(actionableCashflows, interestTradeLegTypes, remainingCashflows);
            groupedCashflows.add(groupedSecondary);
        }

        return groupedCashflows;
    }

    static List<List<CashflowBuilder>> groupForFx(List<CashflowBuilder> cashflowBdrs) {
        List<CashflowBuilder> remainingCashflows = new ArrayList<>();
        Map<TradeLegType, String> requiredTradeLegTypes = new EnumMap<>(TradeLegType.class);
        requiredTradeLegTypes.put(TradeLegType.FX_SIDE1, "v");
        requiredTradeLegTypes.put(TradeLegType.FX_SIDE2, "v");

        return List.of(groupCashflowsForConfirmationMatchRequest(cashflowBdrs, requiredTradeLegTypes, remainingCashflows));
    }

    /// The Map `requiredTradeLegTypes` must be mutable because elements are removed
    /// A new mutable Set of remaining cashflows `remainingCashflows` is needed because the Set `cashflowBuilders` is immutable
    ///
    /// @return unmodifiable list of cashflows
    private static List<CashflowBuilder> groupCashflowsForConfirmationMatchRequest(List<CashflowBuilder> cashflowBuilders, Map<TradeLegType, String> requiredTradeLegTypes, List<CashflowBuilder> remainingCashflows) {
        List<CashflowBuilder> cashflowGroup = new ArrayList<>();

        for (var cashflowBdr : cashflowBuilders) {
            // Verify that the tradeLegTypes required for creating ConfirmationMatchRequest are present
            // If present add to cashflowGroup list. Else add to remainingCashflows list
            TradeLegType tradeLegType = cashflowBdr.tradeLegType();
            String val = requiredTradeLegTypes.remove(tradeLegType);
            if (val == null) {
                remainingCashflows.add(cashflowBdr);
            } else {
                cashflowGroup.add(cashflowBdr);
            }
        }

        // This check prevents infinite loop also
        if (!requiredTradeLegTypes.isEmpty()) {
            var requiredLegTypes = getKeysAsDelimitedString(requiredTradeLegTypes);
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE(
                    "All the required TradeLegTypes are not present for the trade in order to build ConfirmationMatchRequest. The required TradeLegTypes are: " + requiredLegTypes
                    , new ExceptionSubCategory(INVALID_TRADE_LEG_TYPE, cashflowBuilders));
        }

        return Collections.unmodifiableList(cashflowGroup);
    }

    static List<ConfirmationMatchRequestFactoryOutcome> buildConfirmationMatchRequests(List<List<CashflowSet>> groupedCashflows, long tradeId, int tradeVersion, TradeType tradeType) {
        final List<ConfirmationMatchRequestFactoryOutcome> outcomes = new ArrayList<>();

        for (List<CashflowSet> cashflows : groupedCashflows) {
            final List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities = new ArrayList<>();

            outcomes.add(
                    createConfMatchRequest(
                            buildTradeLegMatchAttributes(cashflows, tradeId, tradeVersion, confMatchStatusJpaEntities),
                            tradeId, tradeVersion, tradeType, confMatchStatusJpaEntities));
        }

        return outcomes;
    }

    private static List<TradeLegMatchAttributeAvro> buildTradeLegMatchAttributes(List<CashflowSet> cashflows, long tradeId, int tradeVersion, List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities) {
        List<TradeLegMatchAttributeAvro> tradeLegMatchAttributes = new ArrayList<>();

        // Get confReqId and contraPairReqId. All cashflows in the given list are expected to have the same confReqId. Still, it is verified in further steps
        CashflowSet referencedCashflowSet = cashflows.getFirst();
        // Get confReqId of the newly created cashflow, which is CashflowSet::primaryCashflow
        final Long confReqId = referencedCashflowSet.primaryCashflow().confReqId();
        // Get contraPairReqId from the previous version of the cashflow. If confReqId does not exist assign null
        Cashflow referencedPrevCashflow = switch (referencedCashflowSet) {
            case CashflowSet.InitialVersion _ -> null;
            case CashflowSet.SubsequentVersion(var _, var prevCf, var _) -> prevCf;
            case CashflowSet.CancelledVersion(var _, var prevCf) -> prevCf;
        };
        final Long contraPairReqId = referencedPrevCashflow == null ? null : referencedPrevCashflow.confReqId();

        // Ensure that confReqId was assigned(as of now, assigned only by TradeService)
        if (confReqId == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE(
                    "Attempt to create confirmation match request for a cashflow for which 'confReqId' is not assigned"
                    , new ExceptionSubCategory(NO_CONF_MATCH_ID, referencedCashflowSet.primaryCashflow()));
        }

        // Build TradeLegMatchAttributeAvro
        for (CashflowSet cashflowSet : cashflows) {
            final Cashflow primaryCashflow = cashflowSet.primaryCashflow();

            // Verify that all the cashflows belong to the same trade
            if (primaryCashflow.tradeId() != tradeId && primaryCashflow.tradeVersion() != tradeVersion) {
                throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE(
                        "Invalid group of cashflows received for generating matching request. Only the cashflows that belong to the same trade are expected"
                        , new ExceptionSubCategory(CASHFLOWS_OF_MULTIPLE_TRADES, cashflowSet));
            }

            // Verify that confReqId is the same for all previous cashflows
            Cashflow prevCashflow = switch (cashflowSet) {
                case CashflowSet.InitialVersion _ -> null;
                case CashflowSet.SubsequentVersion(var _, var prevCf, var _) -> prevCf;
                case CashflowSet.CancelledVersion(var _, var prevCf) -> prevCf;
            };
            if (referencedPrevCashflow != null && prevCashflow != null && !referencedPrevCashflow.confReqId().equals(prevCashflow.confReqId())) {
                throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE(
                        "Attempting to create confirmation match request for the same set of cashflows that were requested collectively. But the confReqId differ among the previous cashflows"
                        , new ExceptionSubCategory(CONF_REQ_ID_DIFFER_FOR_AMONG_PREV_CASHFLOWS, List.of(referencedPrevCashflow, prevCashflow)));
            } else if ((referencedPrevCashflow != null && prevCashflow == null) || (referencedPrevCashflow == null && prevCashflow != null)) {
                throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE(
                        "Previous cashflow does not exist for ALL the newly processed cashflows, instead it exist only for some"
                        , new ExceptionSubCategory(PREV_CASHFLOW_NOT_EXIST_FOR_ALL, List.of(cashflows)));
            }

            // Create TradeLegMatchAttribute
            var attribute = createTradeLegMatchAttribute(primaryCashflow);
            tradeLegMatchAttributes.add(attribute);

            // Create ConfirmationMatchStatusJpaEntity
            var matchStatusJpaEntity = createConfirmationMatchStatusJpaEntity(primaryCashflow, confReqId, contraPairReqId);
            confMatchStatusJpaEntities.add(matchStatusJpaEntity);

        }

        return tradeLegMatchAttributes;
    }

    private static ConfirmationMatchStatusEntity createConfirmationMatchStatusJpaEntity(Cashflow subjectCashflow, Long confReqId, Long contraPairReqId) {
        var ent = new ConfirmationMatchStatusEntity();
        ent.setConfRequestId(confReqId);
        ent.setContraPairReqId(contraPairReqId); // could be null and that is ok
        ent.setTradeId(subjectCashflow.tradeId());
        ent.setTradeVersion(subjectCashflow.tradeVersion());
        ent.setTradeLegId(subjectCashflow.tradeLegId());
        ent.setTradeLegVersion(subjectCashflow.tradeLegVersion());
        ent.setMatchEventId(null);
        ent.setMatchEventVersion(null);
        ent.setNostroId(subjectCashflow.nostroId());
        ent.setSsiId(subjectCashflow.ssiId());
        ent.setSentOrRecd(SentOrRecd.SENT);
        ent.setMatchStatus(null);
        ent.setMatchDate(null);
        ent.setInputDateTime(LocalDateTime.now());

        return ent;
    }

    private static TradeLegMatchAttributeAvro createTradeLegMatchAttribute(Cashflow cashflow) {
        long tradeLegId = cashflow.tradeLegId();
        int tradeLegVersion = cashflow.tradeLegVersion();
        String nostroId = cashflow.nostroId();
        String ssiId = cashflow.ssiId();

        TradeLegMatchAttributeAvro attr = new TradeLegMatchAttributeAvro();
        attr.setTradeLegId(tradeLegId);
        attr.setTradeLegVersion(tradeLegVersion);
        attr.setNostroId(nostroId);
        attr.setSsiId(ssiId);
        attr.setValueDate(cashflow.valueDate());

        return attr;
    }

    private static String getKeysAsDelimitedString(Map<TradeLegType, String> tradeLegMatchAttributes) {
        return tradeLegMatchAttributes.keySet().stream().map(TradeLegType::name).collect(Collectors.joining(","));
    }

    private static ConfirmationMatchRequestFactoryOutcome createConfMatchRequest(List<TradeLegMatchAttributeAvro> tradeLegMatchAttributes, long tradeId, int tradeVersion, TradeType tradeType, List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities) {
        var matchReq = new ConfirmationMatchRequestAvro();
        matchReq.setTradeId(tradeId);
        matchReq.setTradeVersion(tradeVersion);
        matchReq.setTradeType(tradeType.name());
        matchReq.setTradeLegMatchAttributes(tradeLegMatchAttributes);

        // All confMatchStatusEntities in the given list have the same confRequestId and contraPairReqId. So just taking the first jpa entity is fine
        ConfirmationMatchStatusEntity confMatchStatusEntity = confMatchStatusJpaEntities.getFirst();
        // Set RequestId which was already computed when confirmation eligibility check was performed(as of now checked in TradeService)
        matchReq.setRequestId(confMatchStatusEntity.getConfRequestId());
        // Set contraPairId of prevCashflow
        matchReq.setContraPairId(confMatchStatusEntity.getContraPairReqId()); // could be null which is fine

        return new ConfirmationMatchRequestFactoryOutcome(matchReq, confMatchStatusJpaEntities);
    }
}
