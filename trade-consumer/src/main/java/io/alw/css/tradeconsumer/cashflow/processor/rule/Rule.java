package io.alw.css.tradeconsumer.cashflow.processor.rule;

import io.alw.css.domain.common.RevisionType;
import io.alw.css.domain.common.TradeType;

import java.util.List;

public record Rule(
        TradeType tradeType,
        RevisionType result,
        CashflowSequence cashflowSequence,
        List<TradeEventAndAction> tradeEventAndActionRecords) {
}
