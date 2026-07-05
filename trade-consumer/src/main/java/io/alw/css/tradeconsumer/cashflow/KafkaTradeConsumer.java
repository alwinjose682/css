package io.alw.css.tradeconsumer.cashflow;

import io.alw.css.domain.common.InputBy;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.tradeconsumer.cashflow.service.TradeService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaTradeConsumer {
    private final TradeService tradeService;

    public KafkaTradeConsumer(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @KafkaListener(topics = "${app.kafka.topic.trade-input}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "tradeMessageListenerContainerFactory")
    public void accept(Message<TradeAvro> message) {
        tradeService.process(message.getPayload(), InputBy.CSS_SYS);
    }
}
