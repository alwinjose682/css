package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.TradeLink;
import io.alw.css.domain.cashflow.TradeLinkBuilder;
import io.alw.datagen.TestDataGeneratable;

import java.util.List;

public sealed interface MessageContext extends TestDataGeneratable permits FxCashMessageContext, MmCashMessageContext {
    FoCashMessage rootFoCashMessage();

    void setRootFoCashMessage(FoCashMessage rootFoCashMessage);

    <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs);
}
