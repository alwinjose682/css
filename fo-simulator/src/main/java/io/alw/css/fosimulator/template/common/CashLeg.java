package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.CashLegType;

public sealed interface CashLeg permits MmCashLeg {
    CashLegType cashLegType();
    FoCashMessage cashMessage();
    void setCashMessage(FoCashMessage cashMessage);
}
