package io.alw.css.tradepublisher.confirmation;

import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradepublisher.confirmation.service.ConfirmationMatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private final ConfirmationMatchService confirmationMatchService;

    public KafkaConsumer(ConfirmationMatchService confirmationMatchService) {
        this.confirmationMatchService = confirmationMatchService;
    }

    @KafkaListener(topics = "${app.kafka.topic.trade-match-request}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "tradeMatchRequestListenerContainerFactory")
    public void consume(Message<ConfirmationMatchRequestAvro> msg) {
        confirmationMatchService.processMatchRequest(msg.getPayload());
    }
}
