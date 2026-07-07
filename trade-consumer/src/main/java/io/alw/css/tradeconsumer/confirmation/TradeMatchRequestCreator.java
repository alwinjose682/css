package io.alw.css.tradeconsumer.confirmation;

import io.alw.css.confirmation.TradeLegMatchAttribute;
import io.alw.css.confirmation.TradeMatchRequest;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.domain.trade.TradeLegType;

import java.util.*;
import java.util.stream.Collectors;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.CASHFLOWS_OF_MULTIPLE_TRADES;
import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.INVALID_TRADE_LEG_TYPE;

public class TradeMatchRequestCreator {

    public static List<TradeMatchRequest> forMmTerm(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType) {
        Map<TradeLegType, String> primaryTradeLegTypes = new EnumMap<>(TradeLegType.class);
        primaryTradeLegTypes.put(TradeLegType.MM_PRINCIPAL, "v");
        primaryTradeLegTypes.put(TradeLegType.MM_MATURITY, "v");

        return forGenericMm(cashflows, tradeId, tradeVersion, tradeType, primaryTradeLegTypes);
    }

    public static List<TradeMatchRequest> forMmCall(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType) {
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

    private static List<TradeMatchRequest> forGenericMm(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType, Map<TradeLegType, String> primaryTradeLegTypes) {
        List<TradeMatchRequest> tradeMatchRequests = new ArrayList<>();
        Set<Cashflow> remainingCashflows = new HashSet<>();

        // For Principal and Maturity legs
        tradeMatchRequests.add(
                createTradeMatchRequest(
                        buildTradeLegMatchAttributes(cashflows, primaryTradeLegTypes, tradeId, tradeVersion, remainingCashflows),
                        tradeId, tradeVersion, tradeType));

        // For Interest legs
        while (!remainingCashflows.isEmpty()) {
            Map<TradeLegType, String> interestTradeLegTypes = new EnumMap<>(TradeLegType.class);
            interestTradeLegTypes.put(TradeLegType.MM_INTEREST, "v");
            var actionableCashflows = remainingCashflows;
            remainingCashflows = new HashSet<>();

            tradeMatchRequests.add(
                    createTradeMatchRequest(
                            buildTradeLegMatchAttributes(actionableCashflows, interestTradeLegTypes, tradeId, tradeVersion, remainingCashflows),
                            tradeId, tradeVersion, tradeType));

            return tradeMatchRequests;
        }

        return tradeMatchRequests;
    }

    public static List<TradeMatchRequest> forFx(Set<Cashflow> cashflows, long tradeId, int tradeVersion, TradeType tradeType) {
        List<TradeMatchRequest> tradeMatchRequests = new ArrayList<>();
        Set<Cashflow> remainingCashflows = new HashSet<>();
        Map<TradeLegType, String> requiredTradeLegTypes = new EnumMap<>(TradeLegType.class);
        requiredTradeLegTypes.put(TradeLegType.FX_SIDE1, "v");
        requiredTradeLegTypes.put(TradeLegType.FX_SIDE2, "v");

        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = buildTradeLegMatchAttributes(cashflows, requiredTradeLegTypes, tradeId, tradeVersion, remainingCashflows);
        tradeMatchRequests.add(createTradeMatchRequest(tradeLegMatchAttributes, tradeId, tradeVersion, tradeType));

        return tradeMatchRequests;
    }

    /// The Map `requiredTradeLegTypes` must be mutable because elements are removed
    /// A new mutable Set of remaining cashflows `remainingCashflows` is needed because the Set `allCashflows` is immutable
    private static Set<TradeLegMatchAttribute> buildTradeLegMatchAttributes(Set<Cashflow> allCashflows, Map<TradeLegType, String> requiredTradeLegTypes, long tradeId, int tradeVersion, Set<Cashflow> remainingCashflows) {
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes = new HashSet<>();
        for (Cashflow cashflow : allCashflows) {
            long tradeLegId = cashflow.tradeLegId();
            int tradeLegVersion = cashflow.tradeLegVersion();
            String nostroId = cashflow.nostroId();
            String ssiId = cashflow.ssiId();
            TradeLegType tradeLegType = cashflow.tradeLegType();

            // Verify that all the cashflows belong to the same trade
            if (cashflow.tradeId() != tradeId && cashflow.tradeVersion() != tradeVersion) {
                throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Invalid group of cashflows received for generating matching request. Only the cashflows that belong to the same trade are expected", new ExceptionSubCategory(CASHFLOWS_OF_MULTIPLE_TRADES, cashflow));
            }

            // Verify that the tradeLegTypes required for creating TradeMatchRequest are present
            String val = requiredTradeLegTypes.remove(tradeLegType);
            if (val == null) {
                remainingCashflows.add(cashflow);
            } else {
                TradeLegMatchAttribute attribute = new TradeLegMatchAttribute(tradeLegId, tradeLegVersion, nostroId, ssiId, cashflow.valueDate());
                tradeLegMatchAttributes.add(attribute);
            }
        }

        // This check also prevents infinite loop
        if (!requiredTradeLegTypes.isEmpty()) {
            var requiredLegTypes = getKeysAsDelimitedString(requiredTradeLegTypes);
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("All the required TradeLegTypes are not present for the trade in order to build TradeMatchRequest. The required TradeLegTypes are: " + requiredLegTypes, new ExceptionSubCategory(INVALID_TRADE_LEG_TYPE, allCashflows));
        }

        return tradeLegMatchAttributes;
    }

    private static String getKeysAsDelimitedString(Map<TradeLegType, String> tradeLegMatchAttributes) {
        return tradeLegMatchAttributes.keySet().stream().map(TradeLegType::name).collect(Collectors.joining(","));
    }

    private static TradeMatchRequest createTradeMatchRequest(Set<TradeLegMatchAttribute> tradeLegMatchAttributes, long tradeId, int tradeVersion, TradeType tradeType) {
        return new TradeMatchRequest(tradeId, tradeVersion, tradeLegMatchAttributes, tradeType);
    }
}
