package io.alw.css.tradepublisher.trade;

import io.alw.css.domain.trade.Trade;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.tradepublisher.CssTaskExecutor;
import io.alw.css.tradepublisher.properties.KafkaTopicProperties;
import io.alw.css.tradepublisher.trade.mapper.TradeAvroMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.function.Consumer;

public class TradePublisher implements Consumer<List<Trade>> {
    private final static Logger log = LoggerFactory.getLogger(TradePublisher.class);
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaTemplate<String, TradeAvro> kafkaTemplateTradeCashGenerationEvent;
    private final CssTaskExecutor cssTaskExecutor;

    public TradePublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, TradeAvro> kafkaTemplateTradeCashGenerationEvent, CssTaskExecutor cssTaskExecutor) {
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.kafkaTemplateTradeCashGenerationEvent = kafkaTemplateTradeCashGenerationEvent;
        this.cssTaskExecutor = cssTaskExecutor;
    }

    @Override
    public void accept(List<Trade> trades) {
        trades.forEach(this::publish);
    }

    public void publish(Trade trdMsg) {
        String outputTopic = kafkaTopicProperties.tradeOutputTopic();
        TradeAvro avroMsg = TradeAvroMapper.instance().domainToAvro(trdMsg);
        String key = String.valueOf(avroMsg.getTradeID());
        String tradeIdVer = avroMsg.getTradeID() + "-" + avroMsg.getTradeVersion();
        log.trace("Sending trade message: {} to topic: {}", tradeIdVer, outputTopic);

        kafkaTemplateTradeCashGenerationEvent
                .send(outputTopic, key, avroMsg)
                .whenCompleteAsync((result, e) -> {
                    if (e == null) {
                        RecordMetadata recordMetadata = result.getRecordMetadata();
                        log.info("Published Trade message[{}] to topic: {}, partition: {}, offset: {}", tradeIdVer, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                    } else {
                        log.error("An error occurred when publishing Trade message[{}] to kafka topic: {}. Message: {}", tradeIdVer, outputTopic, avroMsg);
                    }
                }, cssTaskExecutor.executor());
    }
}
