package io.alw.css.tradepublisher.confirmation.service;

import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradepublisher.confirmation.mapper.ConfirmationMatchRequestMapper;
import io.alw.css.tradepublisher.confirmation.template.ConfirmationMatchEventTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationMatchService {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationMatchService.class);
    private final ConfirmationMatchEventTemplate confirmationMatchEventTemplate;

    public ConfirmationMatchService(ConfirmationMatchEventTemplate confirmationMatchEventTemplate) {
        this.confirmationMatchEventTemplate = confirmationMatchEventTemplate;
    }

    public void processMatchRequest(ConfirmationMatchRequestAvro matchRequestAvro) {
        long tradeId = matchRequestAvro.getTradeId();
        int tradeVersion = matchRequestAvro.getTradeVersion();
        log.info("Received ConfirmationMatchRequestAvro[tradeId: {}, tradeVersion: {}] for confirmation message generation and matching", tradeId, tradeVersion);

        ConfirmationMatchRequest req = ConfirmationMatchRequestMapper.instance().avroToDomain(matchRequestAvro);
        confirmationMatchEventTemplate.consume(req);
    }
}
