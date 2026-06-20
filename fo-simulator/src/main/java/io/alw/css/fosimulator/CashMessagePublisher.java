package io.alw.css.fosimulator;

import io.alw.css.domain.trade.Trade;
import io.alw.css.fosimulator.mapper.TradeAvroMapper;
import io.alw.css.fosimulator.model.properties.KafkaTopicProperties;
import io.alw.css.serialization.trade.TradeAvro;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Set;
import java.util.function.Consumer;

public class CashMessagePublisher implements Consumer<Set<Trade>> {
    private final static Logger log = LoggerFactory.getLogger(CashMessagePublisher.class);
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaTemplate<String, TradeAvro> kafkaTemplateCashMessage;
    private final CssTaskExecutor cssTaskExecutor;

    public CashMessagePublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, TradeAvro> kafkaTemplateCashMessage, CssTaskExecutor cssTaskExecutor) {
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.kafkaTemplateCashMessage = kafkaTemplateCashMessage;
        this.cssTaskExecutor = cssTaskExecutor;
    }

    @Override
    public void accept(Set<Trade> trades) {
        trades.forEach(this::publish);
    }

    public void publish(Trade trdMsg) {
        String outputTopic = kafkaTopicProperties.cashMessageOutputTopic();
        TradeAvro avroMsg = TradeAvroMapper.instance().domainToAvro(trdMsg);
        String key = avroMsg.getTradeID() + "-" + avroMsg.getTradeVersion();
        log.trace("Sending trade message: {} to topic: {}", key, outputTopic);

        kafkaTemplateCashMessage
                .send(outputTopic, key, avroMsg)
                .whenCompleteAsync((result, e) -> {
                    if (e == null) {
                        RecordMetadata recordMetadata = result.getRecordMetadata();
                        log.info("Published trade message[{}] to topic: {}, partition: {}, offset: {}", key, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                    } else {
                        log.error("An error occurred when publishing trade message[{}] to kafka topic: {}. Message: {}", key, outputTopic, avroMsg);
                    }
                }, cssTaskExecutor.executor());
    }
}
