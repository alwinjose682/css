package io.alw.css.tradeconsumer.confirmation.domain;

import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.common.SentOrRecd;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.serialization.confirmation.TradeLegMatchAttributeAvro;
import io.alw.css.tradeconsumer.confirmation.model.ConfirmationMatchRequestFactoryOutcome;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.CASHFLOWS_OF_MULTIPLE_TRADES;
import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.INVALID_TRADE_LEG_TYPE;

public class ConfirmationMatchRequestFactory {

    public static List<ConfirmationMatchRequestFactoryOutcome> forMmTerm(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType) {
        Map<TradeLegType, String> primaryTradeLegTypes = new EnumMap<>(TradeLegType.class);
        primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
        primaryTradeLegTypes.put(TradeLegType.MM_MATURITY, "v");

        return forGenericMm(cashflows, tradeId, tradeVersion, tradeType, primaryTradeLegTypes);
    }

    public static List<ConfirmationMatchRequestFactoryOutcome> forMmCall(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType) {
        boolean hasMaturityLeg = cashflows.stream().anyMatch(cf -> cf.tradeLegType() == TradeLegType.MM_MATURITY);
        Map<TradeLegType, String> primaryTradeLegTypes = new EnumMap<>(TradeLegType.class);
        if (hasMaturityLeg) {
            primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
            primaryTradeLegTypes.put(TradeLegType.MM_MATURITY, "v");
        } else {
            primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
        }

        return forGenericMm(cashflows, tradeId, tradeVersion, tradeType, primaryTradeLegTypes);
    }

    private static List<ConfirmationMatchRequestFactoryOutcome> forGenericMm(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType, Map<TradeLegType, String> primaryTradeLegTypes) {
        final List<ConfirmationMatchRequestFactoryOutcome> outcomes = new ArrayList<>();
        final List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities = new ArrayList<>();
        Set<Cashflow> remainingCashflows = new HashSet<>();

        // For Principal and Maturity legs
        outcomes.add(
                createConfMatchRequest(
                        buildTradeLegMatchAttributes(cashflows, primaryTradeLegTypes, tradeId, tradeVersion, remainingCashflows, confMatchStatusJpaEntities),
                        tradeId, tradeVersion, tradeType, confMatchStatusJpaEntities));

        // For Interest legs
        while (!remainingCashflows.isEmpty()) {
            final List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntitiesIntr = new ArrayList<>();
            Map<TradeLegType, String> interestTradeLegTypes = new EnumMap<>(TradeLegType.class);
            interestTradeLegTypes.put(TradeLegType.MM_INTEREST, "v");
            var actionableCashflows = remainingCashflows;
            remainingCashflows = new HashSet<>();

            outcomes.add(
                    createConfMatchRequest(
                            buildTradeLegMatchAttributes(actionableCashflows, interestTradeLegTypes, tradeId, tradeVersion, remainingCashflows, confMatchStatusJpaEntitiesIntr),
                            tradeId, tradeVersion, tradeType, confMatchStatusJpaEntitiesIntr));
        }

        return outcomes;
    }

    public static List<ConfirmationMatchRequestFactoryOutcome> forFx(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType) {
        final List<ConfirmationMatchRequestFactoryOutcome> outcomes = new ArrayList<>();
        final List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities = new ArrayList<>();
        Set<Cashflow> remainingCashflows = new HashSet<>();
        Map<TradeLegType, String> requiredTradeLegTypes = new EnumMap<>(TradeLegType.class);
        requiredTradeLegTypes.put(TradeLegType.FX_SIDE1, "v");
        requiredTradeLegTypes.put(TradeLegType.FX_SIDE2, "v");

        List<TradeLegMatchAttributeAvro> tradeLegMatchAttributes = buildTradeLegMatchAttributes(cashflows, requiredTradeLegTypes, tradeId, tradeVersion, remainingCashflows, confMatchStatusJpaEntities);
        outcomes.add(createConfMatchRequest(tradeLegMatchAttributes, tradeId, tradeVersion, tradeType, confMatchStatusJpaEntities));

        return outcomes;
    }

    /// The Map `requiredTradeLegTypes` must be mutable because elements are removed
    /// A new mutable Set of remaining cashflows `remainingCashflows` is needed because the Set `allCashflows` is immutable
    private static List<TradeLegMatchAttributeAvro> buildTradeLegMatchAttributes(Set<Cashflow> allCashflows, Map<TradeLegType, String> requiredTradeLegTypes, long tradeId, int tradeVersion, Set<Cashflow> remainingCashflows, List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities) {
        List<TradeLegMatchAttributeAvro> tradeLegMatchAttributes = new ArrayList<>();
        for (Cashflow cashflow : allCashflows) {
            TradeLegType tradeLegType = cashflow.tradeLegType();

            // Verify that all the cashflows belong to the same trade
            if (cashflow.tradeId() != tradeId && cashflow.tradeVersion() != tradeVersion) {
                throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Invalid group of cashflows received for generating matching request. Only the cashflows that belong to the same trade are expected", new ExceptionSubCategory(CASHFLOWS_OF_MULTIPLE_TRADES, cashflow));
            }

            // Verify that the tradeLegTypes required for creating ConfirmationMatchRequest are present
            String val = requiredTradeLegTypes.remove(tradeLegType);
            if (val == null) {
                remainingCashflows.add(cashflow);
            } else {
                // Create TradeLegMatchAttribute
                var attribute = createTradeLegMatchttribute(cashflow);
                tradeLegMatchAttributes.add(attribute);

                // Create ConfirmationMatchEventJpaEntity
                var matchStatusJpaEntity = createConfirmationMatchStatusJpaEntity(cashflow);
                confMatchStatusJpaEntities.add(matchStatusJpaEntity);
            }
        }

        // This check prevents infinite loop also
        if (!requiredTradeLegTypes.isEmpty()) {
            var requiredLegTypes = getKeysAsDelimitedString(requiredTradeLegTypes);
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("All the required TradeLegTypes are not present for the trade in order to build ConfirmationMatchRequest. The required TradeLegTypes are: " + requiredLegTypes, new ExceptionSubCategory(INVALID_TRADE_LEG_TYPE, allCashflows));
        }

        return tradeLegMatchAttributes;
    }

    private static ConfirmationMatchStatusEntity createConfirmationMatchStatusJpaEntity(Cashflow cashflow) {
        var ent = new ConfirmationMatchStatusEntity();
        ent.setTradeId(cashflow.tradeId());
        ent.setTradeVersion(cashflow.tradeVersion());
        ent.setTradeLegId(cashflow.tradeLegId());
        ent.setTradeLegVersion(cashflow.tradeLegVersion());
        ent.setMatchEventId(null);
        ent.setMatchEventVersion(null);
        ent.setNostroId(cashflow.nostroId());
        ent.setSsiId(cashflow.ssiId());
        ent.setSentOrRecd(SentOrRecd.SENT);
        ent.setMatchStatus(null);
        ent.setMatchDate(null);
        ent.setInputDateTime(LocalDateTime.now()); // assign here or later or by database ?

        return ent;
    }

    private static TradeLegMatchAttributeAvro createTradeLegMatchttribute(Cashflow cashflow) {
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

        return new ConfirmationMatchRequestFactoryOutcome(matchReq, confMatchStatusJpaEntities);
    }
}
