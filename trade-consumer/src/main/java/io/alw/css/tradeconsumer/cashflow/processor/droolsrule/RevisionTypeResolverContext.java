package io.alw.css.tradeconsumer.cashflow.processor.droolsrule;

import io.alw.css.domain.common.RevisionType;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;

import java.util.Optional;

public class RevisionTypeResolverContext {
    //Facts
    private final boolean firstCashflow;
    private final TradeType tradeType;
    private final TradeEventType tradeEventType;
    private final TradeEventAction tradeEventAction;

    //Result
    private RevisionType result;

    public RevisionTypeResolverContext(boolean firstCashflow, TradeType tradeType, TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        this.firstCashflow = firstCashflow;
        this.tradeType = tradeType;
        this.tradeEventType = tradeEventType;
        this.tradeEventAction = tradeEventAction;
        this.result = null;
    }

    public void setResult(RevisionType result) {
        this.result = result;
    }

    public Optional<RevisionType> result() {
        return Optional.ofNullable(result);
    }

    public boolean firstCashflow() {
        return firstCashflow;
    }

    public TradeType tradeType() {
        return tradeType;
    }

    public TradeEventType tradeEventType() {
        return tradeEventType;
    }

    public TradeEventAction tradeEventAction() {
        return tradeEventAction;
    }
}

