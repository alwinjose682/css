package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;

import java.util.List;

public final class FxCashMessageContext implements MessageContext {
    private FoCashMessage rootFoCashMessage;

    @Override
    public FoCashMessage rootFoCashMessage() {
        return rootFoCashMessage;
    }

    @Override
    public void setRootFoCashMessage(FoCashMessage rootFoCashMessage) {
        this.rootFoCashMessage = rootFoCashMessage;
    }

    @Override
    public <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs) {

    }
}
