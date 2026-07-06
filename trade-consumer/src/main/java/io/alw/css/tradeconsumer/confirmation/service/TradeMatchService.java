package io.alw.css.tradeconsumer.confirmation.service;

import io.alw.css.confirmation.TradeMatchRequest;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.common.TradeType;
import io.alw.css.tradeconsumer.confirmation.TradeMatchRequestCreator;
import io.alw.css.tradeconsumer.confirmation.TradeMatchRequestPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class TradeMatchService {
    private static final Logger log = LoggerFactory.getLogger(TradeMatchService.class);
    private final TradeMatchRequestPublisher tradeMatchRequestPublisher;

    public TradeMatchService(TradeMatchRequestPublisher tradeMatchRequestPublisher) {
        this.tradeMatchRequestPublisher = tradeMatchRequestPublisher;
    }

    /// Sends the processed cashflows for matching with counterparty confirmation. Note: A confirmation message(ex: MT300) is not created
    /// The given List of cashflows may produce multiple TradeMatchRequests. Ex: for a list of Mm Cashflows
    public void sendForMatching(Set<Cashflow> cashflows) {
        Iterator<Cashflow> it = cashflows.iterator();
        final Cashflow cf;
        if (it.hasNext()) {
            cf = it.next();
        } else {
            log.warn("No trades to send for matching because the list of cashflows provided is empty");
            return;
        }
        long tradeId = cf.tradeId();
        int tradeVersion = cf.tradeVersion();
        TradeType tradeType = cf.tradeType();

        // Create TradeMatchRequests
        List<TradeMatchRequest> tradeMatchRequests = switch (tradeType) {
            case FX -> TradeMatchRequestCreator.forFx(cashflows, tradeId, tradeVersion, tradeType);
            case MM_TERM -> TradeMatchRequestCreator.forMmTerm(cashflows, tradeId, tradeVersion, tradeType);
            case MM_CALL -> TradeMatchRequestCreator.forMmCall(cashflows, tradeId, tradeVersion, tradeType);
            case PAYMENT, FX_NDF, BOND, REPO, MM, OPTION -> throw new RuntimeException("No implementation yet for generating TradeMatchRequest for TradeType: " + tradeType);
        };

        // TODO: write database audit record
        tradeMatchRequestPublisher.publish(tradeMatchRequests);
    }
}
