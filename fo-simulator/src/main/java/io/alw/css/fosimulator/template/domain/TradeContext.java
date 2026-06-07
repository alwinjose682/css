package io.alw.css.fosimulator.template.domain;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.datagen.TestDataGeneratable;

import java.util.List;

public sealed interface TradeContext extends TestDataGeneratable permits FxTradeContext, MmTradeContext {
    FoCashMessage rootFoCashMessage();

    void setRootFoCashMessage(FoCashMessage rootFoCashMessage);

    <M extends TradeContext> List<FoCashMessage> mapToCashMessage(List<M> trdCtxs);
}
