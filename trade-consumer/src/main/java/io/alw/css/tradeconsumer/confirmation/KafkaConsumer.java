package io.alw.css.tradeconsumer.confirmation;

import io.alw.css.serialization.confirmation.ConfirmationMatchStatusAvro;
import io.alw.css.tradeconsumer.confirmation.service.TradeConfirmationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private final static Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final TradeConfirmationService tradeConfirmationService;

    public KafkaConsumer(TradeConfirmationService tradeConfirmationService) {
        this.tradeConfirmationService = tradeConfirmationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.confirmation-match-status}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "confMatchStatusListenerContainerFactory")
    public void accept(Message<ConfirmationMatchStatusAvro> message) {
        var avro = message.getPayload();
        long tradeId = avro.getTradeId();
        int tradeVersion = avro.getTradeVersion();
        String tradeType = avro.getTradeType();
        int numOfTradeLegs = avro.getTradeLegMatchAttributes().size();
        log.info("Received TradeAvroMessage[tradeId: {}, tradeVersion: {}, tradeType: {}] with {} trade legs", tradeId, tradeVersion, tradeType, numOfTradeLegs);

        tradeConfirmationService.process(avro);
    }
}
