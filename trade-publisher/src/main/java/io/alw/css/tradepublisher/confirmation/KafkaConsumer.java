package io.alw.css.tradepublisher.confirmation;

import io.alw.css.serialization.confirmation.TradeMatchRequestAvro;
import io.alw.css.tradepublisher.confirmation.service.MatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private final MatchService matchService;

    public KafkaConsumer(MatchService matchService) {
        this.matchService = matchService;
    }

    @KafkaListener(topics = "${app.kafka.topic.trade-match-request}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "tradeMatchRequestListenerContainerFactory")
    public void consume(Message<TradeMatchRequestAvro> msg) {
        matchService.processMatchRequest(msg);
    }
}
