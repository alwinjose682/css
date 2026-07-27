package io.alw.css.tradepublisher.trade.template.domain;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeDetail;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.datagen.DataGeneratable;

/// Important note: The implementing class MUST implement equals and hashcode. Because, [ExtendedTrade] is used in HashMap. See io.alw.css.tradepublisher.template.TradeAmendmentTemplate#createTradeAmendmentDirectives
public sealed interface ExtendedTrade extends DataGeneratable permits TradeLegGeneratableExtendedTrade, FxTrade {
    long nextTradeLegId();

    int resetTradeLegIdProvider();

    void setTrade(Trade trade);

    TradeLeg rootTradeLeg();

    void setRootTradeLeg(TradeLeg rootTradeLeg);

    Trade trade();

    Iterable<TradeLeg> allTradeLegs();

    TradeLeg getTradeLegFrom(TradeDetail tradeDetail);

    default long tradeId() {
        return trade().tradeID();
    }

    default int tradeVersion() {
        return trade().tradeVersion();
    }

    default TradeEventType tradeEventType() {
        return trade().tradeEventType();
    }

    default TradeEventAction tradeEventAction() {
        return trade().tradeEventAction();
    }

    default TradeType tradeType() {
        return trade().tradeType();
    }
}
