package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.TradeLink;
import io.alw.css.domain.cashflow.TradeLinkBuilder;
import io.alw.css.fosimulator.template.CashMessageTemplateHelper;
import io.alw.datagen.TestDataGeneratable;

import java.util.List;

public sealed interface MessageContext extends TestDataGeneratable permits FxCashMessageContext, MmCashMessageContext {
    FoCashMessage rootFoCashMessage();

    String rootFoCashMessageType();

    void setRootFoCashMessage(FoCashMessage rootFoCashMessage);

    <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs);

    default TradeLink rootFoCashMessageTradeLink() {
        return TradeLinkBuilder.TradeLink(
                rootFoCashMessageType(), null,
                rootFoCashMessage().cashflowID(), rootFoCashMessage().cashflowVersion(),
                rootFoCashMessage().tradeID(), rootFoCashMessage().tradeVersion());
    }
}
