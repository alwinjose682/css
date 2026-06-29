package io.alw.css.tradeconsumer.processor.rule;

import io.alw.css.domain.common.TradeEventType;

import java.util.List;

import static io.alw.css.domain.common.RevisionType.*;
import static io.alw.css.domain.common.TradeEventAction.*;
import static io.alw.css.tradeconsumer.processor.rule.CashflowSequence.INITIAL;
import static io.alw.css.tradeconsumer.processor.rule.CashflowSequence.SUBSEQUENT;

public final class CommonRules implements RuleDefinition {
    private final static Rule rule1 = new Rule(null, NEW, INITIAL, List.of(
            new TradeEventAndAction(TradeEventType.NEW_TRADE, ADD),
            new TradeEventAndAction(TradeEventType.REBOOK, ADD),
            new TradeEventAndAction(TradeEventType.AMEND, ADD),
            new TradeEventAndAction(TradeEventType.AMEND, MODIFY),
            new TradeEventAndAction(TradeEventType.CORRECTION, ADD),
            new TradeEventAndAction(TradeEventType.CORRECTION, MODIFY)
    ));
    private final static Rule rule2 = new Rule(null, COR, SUBSEQUENT, List.of(
            new TradeEventAndAction(TradeEventType.AMEND, ADD),
            new TradeEventAndAction(TradeEventType.AMEND, MODIFY),
            new TradeEventAndAction(TradeEventType.BOOK_MOVE, ADD),
            new TradeEventAndAction(TradeEventType.BOOK_MOVE, MODIFY),
            new TradeEventAndAction(TradeEventType.CORRECTION, ADD),
            new TradeEventAndAction(TradeEventType.CORRECTION, MODIFY)
    ));
    private final static Rule rule3 = new Rule(null, CAN, SUBSEQUENT, List.of(
            new TradeEventAndAction(TradeEventType.CANCEL, ADD),
            new TradeEventAndAction(TradeEventType.AMEND, REMOVE),
            new TradeEventAndAction(TradeEventType.CORRECTION, REMOVE)
    ));

    final static List<Rule> rules = List.of(rule1, rule2, rule3);
}
