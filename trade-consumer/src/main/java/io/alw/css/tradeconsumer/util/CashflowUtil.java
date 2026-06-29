package io.alw.css.tradeconsumer.util;

import io.alw.css.domain.cashflow.CashflowConstants;

public final class CashflowUtil {
    public static boolean isInitialVersion(int tradeVersion, int tradeLegVersion) {
        return tradeVersion == CashflowConstants.TRADE_FIRST_VERSION && tradeLegVersion == CashflowConstants.TRADE_FIRST_VERSION;
    }
}
