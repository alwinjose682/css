package io.alw.css.cashflowconsumer.processor.rule;

import io.alw.css.domain.common.TradeEventType;

import java.util.List;

import static io.alw.css.cashflowconsumer.processor.rule.CashflowSequence.BOTH;
import static io.alw.css.domain.common.RevisionType.NEW;
import static io.alw.css.domain.common.TradeEventAction.ADD;
import static io.alw.css.domain.common.TradeType.BOND;

public final class BondRules implements RuleDefinition {
    private final static Rule rule1 = new Rule(BOND, NEW, BOTH, List.of(
            new TradeEventAndAction(TradeEventType.COUPON, ADD),
            new TradeEventAndAction(TradeEventType.MATURE, ADD)
    ));
    static List<Rule> rules = List.of(rule1);
}
