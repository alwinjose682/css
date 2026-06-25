package io.alw.css.cashflowconsumer.processor.rule;

import io.alw.css.domain.common.TradeEventType;

import java.util.List;

import static io.alw.css.cashflowconsumer.processor.rule.CashflowSequence.BOTH;
import static io.alw.css.cashflowconsumer.processor.rule.CashflowSequence.SUBSEQUENT;
import static io.alw.css.domain.common.RevisionType.CAN;
import static io.alw.css.domain.common.RevisionType.NEW;
import static io.alw.css.domain.common.TradeEventAction.ADD;
import static io.alw.css.domain.common.TradeType.OPTION;

public final class OptionRules implements RuleDefinition {
    private final static Rule rule1 = new Rule(OPTION, NEW, BOTH, List.of(
            new TradeEventAndAction(TradeEventType.EXERCISE, ADD)
    ));

    private final static Rule rule2 = new Rule(OPTION, CAN, SUBSEQUENT, List.of(
            new TradeEventAndAction(TradeEventType.KNOCK_OUT, ADD),
            new TradeEventAndAction(TradeEventType.EXPIRE, ADD)
    ));

    static List<Rule> rules = List.of(rule1,rule2);
}
