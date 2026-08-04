package io.alw.css.tradeconsumer.confirmation;

import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradeconsumer.CssTaskExecutor;
import io.alw.css.tradeconsumer.confirmation.model.properties.KafkaTopicProperties;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public final class ConfirmationMatchRequestPublisher {
    private final static Logger log = LoggerFactory.getLogger(ConfirmationMatchRequestPublisher.class);
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateConfMatchRequest;
    private final CssTaskExecutor cssTaskExecutor;

    public ConfirmationMatchRequestPublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateConfMatchRequest, CssTaskExecutor cssTaskExecutor) {
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.kafkaTemplateConfMatchRequest = kafkaTemplateConfMatchRequest;
        this.cssTaskExecutor = cssTaskExecutor;
    }

    public void publish(ConfirmationMatchRequestAvro avro) {
        String outputTopic = kafkaTopicProperties.confirmationMatchRequestTopic();
        String key = String.valueOf(avro.getTradeId());
        long requestId = avro.getRequestId();
        log.trace("Sending ConfirmationMatchRequest event[{}] to topic: {}", requestId, outputTopic);

        kafkaTemplateConfMatchRequest
                .send(outputTopic, key, avro)
                .whenCompleteAsync((result, e) -> {
                    if (e == null) {
                        RecordMetadata recordMetadata = result.getRecordMetadata();
                        log.info("Published ConfirmationMatchRequest event[{}] to topic: {}, partition: {}, offset: {}", requestId, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                    } else {
                        log.error("An error occurred when publishing ConfirmationMatchRequest event[{}] to kafka topic: {}. Event: {}", requestId, outputTopic, avro);
                    }
                }, cssTaskExecutor.executor());
    }
}
