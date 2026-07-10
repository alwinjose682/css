package io.alw.css.tradeconsumer.confirmation;

import io.alw.css.domain.common.InputBy;
import io.alw.css.serialization.confirmation.MatchStatusEventAvro;
import io.alw.css.tradeconsumer.confirmation.service.TradeConfirmationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private final TradeConfirmationService tradeConfirmationService;

    public KafkaConsumer(TradeConfirmationService tradeConfirmationService) {
        this.tradeConfirmationService = tradeConfirmationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.trade-match-status-event}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "matchStatusEventListenerContainerFactory")
    public void accept(Message<MatchStatusEventAvro> message) {
        tradeConfirmationService.process(message.getPayload(), InputBy.CSS_SYS);
    }
}
