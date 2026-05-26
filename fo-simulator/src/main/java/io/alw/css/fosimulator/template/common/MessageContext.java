package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.TradeLink;
import io.alw.datagen.TestDataGeneratable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public sealed interface MessageContext extends TestDataGeneratable permits FxCashMessageContext, MmCashMessageContext {
    FoCashMessage rootFoCashMessage(); //TODO: Make this LazyConstant?

    void setRootFoCashMessage(FoCashMessage rootFoCashMessage);

    <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs);

    List<TradeLink> allTradeLinks();

    void setAllTradeLinks(List<TradeLink> allTradeLinks);
}
