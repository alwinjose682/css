package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;

import java.util.List;

public final class FxCashMessageContext implements MessageContext {

    @Override
    public FoCashMessage foCashMessage() {

    }

    @Override
    public void setFoCashMessage(FoCashMessage foCashMessage) {

    }

    @Override
    public <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs) {

    }

    @Override
    public <M extends MessageContext> M with(FoCashMessage msg) {

    }
}
