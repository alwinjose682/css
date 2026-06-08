package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.datagen.TestDataGeneratable;

public sealed interface TradeContext extends TestDataGeneratable permits FxTradeContext, MmTradeContext {
    FoCashMessage rootFoCashMessage();

    void setRootFoCashMessage(FoCashMessage rootFoCashMessage);
}
