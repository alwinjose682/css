package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;

public sealed interface MessageContext permits MmCashMessageContext {
    FoCashMessage foCashMessage();
}
