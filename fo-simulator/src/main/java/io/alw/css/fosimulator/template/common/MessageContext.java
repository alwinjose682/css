package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.datagen.TestDataGeneratable;

import java.util.List;

public sealed interface MessageContext extends TestDataGeneratable permits FxCashMessageContext, MmCashMessageContext {
    FoCashMessage foCashMessage(); //TODO: Make this LazyConstant?

    void setFoCashMessage(FoCashMessage foCashMessage);

    <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs);

    <M extends MessageContext> M with(FoCashMessage msg);
}
