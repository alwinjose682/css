package io.alw.css.tradepublisher.confirmation.service;

import io.alw.css.confirmation.TradeMatchRequest;
import io.alw.css.serialization.confirmation.TradeMatchRequestAvro;
import io.alw.css.tradepublisher.confirmation.mapper.TradeMatchRequestMapper;
import io.alw.css.tradepublisher.confirmation.template.MatchStatusEventTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MatchService {
    private static final Logger log = LoggerFactory.getLogger(MatchService.class);
    private final MatchStatusEventTemplate matchStatusEventTemplate;

    public MatchService(MatchStatusEventTemplate matchStatusEventTemplate) {
        this.matchStatusEventTemplate = matchStatusEventTemplate;
    }

    public void processMatchRequest(TradeMatchRequestAvro matchRequestAvro) {
        long tradeId = matchRequestAvro.getTradeId();
        int tradeVersion = matchRequestAvro.getTradeVersion();
        log.info("Received TradeMatchRequestAvro[tradeId: {}, tradeVersion: {}] for confirmation message generation and matching", tradeId, tradeVersion);

        TradeMatchRequest req = TradeMatchRequestMapper.instance().avroToDomain(matchRequestAvro);
        matchStatusEventTemplate.consume(req);
    }
}
