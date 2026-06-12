package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.common.PayOrReceive;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.domain.trade.TradeLegType;
import io.alw.css.fosimulator.template.model.InterestLegContext;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InterestTradeLeg extends TradeLeg {
    private InterestLegContext interestLegContext;

    public InterestTradeLeg(long tradeLegId, int tradeLegVersion, TradeLegType tradeLegType, TradeEventType tradeEventType, TradeEventAction tradeEventAction, BigDecimal rate, LocalDate valueDate, PayOrReceive payOrReceive, BigDecimal amount, String currCode) {
        super(tradeLegId, tradeLegVersion, tradeLegType, tradeEventType, tradeEventAction, rate, valueDate, payOrReceive, amount, currCode);
    }

    public InterestLegContext interestLegContext() {
        return interestLegContext;
    }

    public void setInterestLegContext(InterestLegContext interestLegContext) {
        this.interestLegContext = interestLegContext;
    }
}
