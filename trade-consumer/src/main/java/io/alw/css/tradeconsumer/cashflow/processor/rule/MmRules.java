package io.alw.css.tradeconsumer.cashflow.processor.rule;

import io.alw.css.domain.common.TradeEventType;

import java.util.List;

import static io.alw.css.domain.common.RevisionType.COR;
import static io.alw.css.domain.common.RevisionType.NEW;
import static io.alw.css.domain.common.TradeEventAction.ADD;
import static io.alw.css.domain.common.TradeType.MM;
import static io.alw.css.tradeconsumer.cashflow.processor.rule.CashflowSequence.SUBSEQUENT;

public final class MmRules implements RuleDefinition {
    private final static Rule rule1 = new Rule(MM, COR, SUBSEQUENT, List.of(
            new TradeEventAndAction(TradeEventType.ROLL, ADD),
            new TradeEventAndAction(TradeEventType.TERMINATE, ADD)
    ));

    private static final Rule rule2 = new Rule(MM, NEW, SUBSEQUENT, List.of(
            new TradeEventAndAction(TradeEventType.INTEREST_ACTION, ADD)
    ));

    static List<Rule> rules = List.of(rule1, rule2);
}
