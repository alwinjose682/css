package io.alw.css.tradepublisher.confirmation;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.serialization.confirmation.ConfirmationMatchEventAvro;
import io.alw.css.tradepublisher.CssTaskExecutor;
import io.alw.css.tradepublisher.confirmation.mapper.ConfirmationMatchEventMapper;
import io.alw.css.tradepublisher.properties.KafkaTopicProperties;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.function.Consumer;

public final class ConfirmationMatchEventPublisher implements Consumer<List<ConfirmationMatchEvent>> {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationMatchEventPublisher.class);
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaTemplate<String, ConfirmationMatchEventAvro> kafkaTemplateConfMatchEvent;
    private final CssTaskExecutor cssTaskExecutor;

    public ConfirmationMatchEventPublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, ConfirmationMatchEventAvro> kafkaTemplateConfMatchEvent, CssTaskExecutor cssTaskExecutor) {
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.kafkaTemplateConfMatchEvent = kafkaTemplateConfMatchEvent;
        this.cssTaskExecutor = cssTaskExecutor;
    }

    @Override
    public void accept(List<ConfirmationMatchEvent> events) {
        events.forEach(this::publish);
    }

    public void publish(ConfirmationMatchEvent event) {
        String outputTopic = kafkaTopicProperties.confirmationMatchEventTopic();
        ConfirmationMatchEventAvro avroMsg = ConfirmationMatchEventMapper.instance().domainToAvro(event);
        String key = String.valueOf(avroMsg.getTradeId());
        log.trace("Sending ConfirmationMatchEvent {} to topic: {}", key, outputTopic);

        kafkaTemplateConfMatchEvent
                .send(outputTopic, key, avroMsg)
                .whenCompleteAsync((result, e) -> {
                    if (e == null) {
                        RecordMetadata recordMetadata = result.getRecordMetadata();
                        log.info("Published ConfirmationMatchEvent[{}] to topic: {}, partition: {}, offset: {}", key, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                    } else {
                        log.error("An error occurred when publishing ConfirmationMatchEvent[{}] to kafka topic: {}. Message: {}", key, outputTopic, avroMsg);
                    }
                }, cssTaskExecutor.executor());
    }
}
