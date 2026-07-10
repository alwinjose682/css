package io.alw.css.tradeconsumer.confirmation;

import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradeconsumer.CssTaskExecutor;
import io.alw.css.tradeconsumer.confirmation.mapper.ConfirmationMatchRequestMapper;
import io.alw.css.tradeconsumer.confirmation.model.properties.KafkaTopicProperties;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

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

    public void publish(List<ConfirmationMatchRequest> confMatchRequests) {
        String outputTopic = kafkaTopicProperties.tradeMatchRequestTopic();
        for (ConfirmationMatchRequest confMatchRequest : confMatchRequests) {
            ConfirmationMatchRequestAvro avro = ConfirmationMatchRequestMapper.instance().domainToAvro(confMatchRequest);
            String key = String.valueOf(avro.getTradeId());
            log.trace("Sending ConfirmationMatchRequest message: {} to topic: {}", key, outputTopic);

            kafkaTemplateConfMatchRequest
                    .send(outputTopic, key, avro)
                    .whenCompleteAsync((result, e) -> {
                        if (e == null) {
                            RecordMetadata recordMetadata = result.getRecordMetadata();
                            log.info("Published ConfirmationMatchRequest message[{}] to topic: {}, partition: {}, offset: {}", key, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                        } else {
                            log.error("An error occurred when publishing ConfirmationMatchRequest message[{}] to kafka topic: {}. Message: {}", key, outputTopic, avro);
                        }
                    }, cssTaskExecutor.executor());
        }
    }
}
