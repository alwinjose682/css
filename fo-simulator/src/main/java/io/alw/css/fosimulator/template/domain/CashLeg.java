package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;

public sealed interface CashLeg permits MmCashLeg {
    CashLegType cashLegType();
    FoCashMessage cashMessage();
    void setCashMessage(FoCashMessage cashMessage);
    TradeContext tradeContext();
    /// The tradeContext should be assignable only once. Implementation should ensure this
    void setTradeContext(TradeContext trdCtx);
}
