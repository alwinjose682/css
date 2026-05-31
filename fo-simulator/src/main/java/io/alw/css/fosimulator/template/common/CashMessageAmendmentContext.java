package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;

import java.util.function.Consumer;

public record CashMessageAmendmentContext(
        RootAmendedCashMessageContext rootAmendedMsgCtx,
        FoCashMessage amendmentSubject,
        String amendmentSubjectLinkType,
        Consumer<FoCashMessage> callback
) {
}
