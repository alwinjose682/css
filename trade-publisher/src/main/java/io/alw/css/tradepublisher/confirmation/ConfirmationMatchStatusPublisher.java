package io.alw.css.tradepublisher.confirmation;

import io.alw.css.confirmation.ConfirmationMatchStatus;
import io.alw.css.serialization.confirmation.ConfirmationMatchStatusAvro;
import io.alw.css.tradepublisher.CssTaskExecutor;
import io.alw.css.tradepublisher.confirmation.mapper.ConfirmationMatchStatusMapper;
import io.alw.css.tradepublisher.properties.KafkaTopicProperties;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.function.Consumer;

public final class ConfirmationMatchStatusPublisher implements Consumer<List<ConfirmationMatchStatus>> {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationMatchStatusPublisher.class);
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaTemplate<String, ConfirmationMatchStatusAvro> kafkaTemplateMatchStatusEvent;
    private final CssTaskExecutor cssTaskExecutor;

    public ConfirmationMatchStatusPublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, ConfirmationMatchStatusAvro> kafkaTemplateMatchStatusEvent, CssTaskExecutor cssTaskExecutor) {
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.kafkaTemplateMatchStatusEvent = kafkaTemplateMatchStatusEvent;
        this.cssTaskExecutor = cssTaskExecutor;
    }

    @Override
    public void accept(List<ConfirmationMatchStatus> events) {
        events.forEach(this::publish);
    }

    public void publish(ConfirmationMatchStatus event) {
        String outputTopic = kafkaTopicProperties.tradeMatchStatusEvent();
        ConfirmationMatchStatusAvro avroMsg = ConfirmationMatchStatusMapper.instance().domainToAvro(event);
        String key = String.valueOf(avroMsg.getTradeId());
        log.trace("Sending MatchStatusEvent {} to topic: {}", key, outputTopic);

        kafkaTemplateMatchStatusEvent
                .send(outputTopic, key, avroMsg)
                .whenCompleteAsync((result, e) -> {
                    if (e == null) {
                        RecordMetadata recordMetadata = result.getRecordMetadata();
                        log.info("Published MatchStatusEvent[{}] to topic: {}, partition: {}, offset: {}", key, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                    } else {
                        log.error("An error occurred when publishing MatchStatusEvent[{}] to kafka topic: {}. Message: {}", key, outputTopic, avroMsg);
                    }
                }, cssTaskExecutor.executor());
    }
}
