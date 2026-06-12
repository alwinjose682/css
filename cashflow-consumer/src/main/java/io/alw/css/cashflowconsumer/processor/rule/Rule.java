package io.alw.css.cashflowconsumer.processor.rule;

import io.alw.css.domain.common.RevisionType;
import io.alw.css.domain.common.TradeType;

import java.util.List;

public record Rule(
        TradeType tradeType,
        RevisionType result,
        CashflowOrder cashflowOrder,
        List<TradeEventAndAction> tradeEventAndActionRecords) {
}
