package io.alw.css.tradepublisher.confirmation.service;

import io.alw.css.serialization.confirmation.TradeMatchRequestAvro;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    public void processMatchRequest(Message<TradeMatchRequestAvro> matchRequestAvro) {
    }
}
