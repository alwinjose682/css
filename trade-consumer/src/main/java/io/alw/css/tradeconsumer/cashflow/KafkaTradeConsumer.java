package io.alw.css.tradeconsumer.cashflow;

import io.alw.css.domain.common.InputBy;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.tradeconsumer.cashflow.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaTradeConsumer {
    private final static Logger log = LoggerFactory.getLogger(KafkaTradeConsumer.class);
    private final TradeService tradeService;

    public KafkaTradeConsumer(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @KafkaListener(topics = "${app.kafka.topic.trade-cash-generation-event}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "tradeCashGenerationListenerContainerFactory")
    public void accept(Message<TradeAvro> message) {
        var tradeAvro = message.getPayload();
        final long tradeId = tradeAvro.getTradeID();
        final int tradeVersion = tradeAvro.getTradeVersion();
        final String tradeType = tradeAvro.getTradeType();
        final int numOfTradeLegs = tradeAvro.getTradeLegs().size();
        log.info("Received TradeAvroMessage[tradeId: {}, tradeVersion: {}, tradeType: {}] with {} trade legs", tradeId, tradeVersion, tradeType, numOfTradeLegs);

        tradeService.process(tradeAvro, InputBy.CSS_SYS);
    }
}
