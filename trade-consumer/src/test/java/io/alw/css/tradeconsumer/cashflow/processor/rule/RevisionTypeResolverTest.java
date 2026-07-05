package io.alw.css.tradeconsumer.cashflow.processor.rule;

import io.alw.css.domain.common.RevisionType;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RevisionTypeResolverTest {

    @Test
    void testCommonRules_whenFirstCashflow_partialTest() {
        RevisionType revisionType = RevisionTypeResolver.resolve(true, TradeType.OPTION, TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        assertEquals(RevisionType.NEW, revisionType);

        revisionType = RevisionTypeResolver.resolve(true, TradeType.REPO, TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        assertEquals(RevisionType.NEW, revisionType);

        revisionType = RevisionTypeResolver.resolve(true, TradeType.FX_NDF, TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        assertEquals(RevisionType.NEW, revisionType);

        revisionType = RevisionTypeResolver.resolve(true, TradeType.BOND, TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        assertEquals(RevisionType.NEW, revisionType);

        revisionType = RevisionTypeResolver.resolve(true, TradeType.PAYMENT, TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        assertEquals(RevisionType.NEW, revisionType);

        revisionType = RevisionTypeResolver.resolve(true, TradeType.FX, TradeEventType.NEW_TRADE, TradeEventAction.ADD);
        assertEquals(RevisionType.NEW, revisionType);
    }
}