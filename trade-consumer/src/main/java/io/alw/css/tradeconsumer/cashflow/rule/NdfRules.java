package io.alw.css.tradeconsumer.cashflow.rule;

import io.alw.css.domain.common.TradeEventType;

import java.util.List;

import static io.alw.css.domain.common.RevisionType.CAN;
import static io.alw.css.domain.common.RevisionType.NEW;
import static io.alw.css.domain.common.TradeEventAction.ADD;
import static io.alw.css.domain.common.TradeType.FX_NDF;
import static io.alw.css.tradeconsumer.cashflow.rule.CashflowSequence.BOTH;
import static io.alw.css.tradeconsumer.cashflow.rule.CashflowSequence.SUBSEQUENT;

public final class NdfRules implements RuleDefinition {
    private final static Rule rule1 = new Rule(FX_NDF, NEW, BOTH, List.of(
            new TradeEventAndAction(TradeEventType.FIX, ADD)
    ));

    private final static Rule rule2 = new Rule(FX_NDF, CAN, SUBSEQUENT, List.of(
            new TradeEventAndAction(TradeEventType.UN_FIX, ADD)
    ));

    final static List<Rule> rules = List.of(rule1, rule2);
}
