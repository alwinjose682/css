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
    private final KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateMatchRequest;
    private final CssTaskExecutor cssTaskExecutor;

    public ConfirmationMatchRequestPublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateMatchRequest, CssTaskExecutor cssTaskExecutor) {
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.kafkaTemplateMatchRequest = kafkaTemplateMatchRequest;
        this.cssTaskExecutor = cssTaskExecutor;
    }

    public void publish(List<ConfirmationMatchRequest> tradeMatchRequests) {
        String outputTopic = kafkaTopicProperties.tradeMatchRequestTopic();
        for (ConfirmationMatchRequest tradeMatchRequest : tradeMatchRequests) {
            ConfirmationMatchRequestAvro avro = ConfirmationMatchRequestMapper.instance().domainToAvro(tradeMatchRequest);
            String key = String.valueOf(avro.getTradeId());
            log.trace("Sending trade message: {} to topic: {}", key, outputTopic);

            kafkaTemplateMatchRequest
                    .send(outputTopic, key, avro)
                    .whenCompleteAsync((result, e) -> {
                        if (e == null) {
                            RecordMetadata recordMetadata = result.getRecordMetadata();
                            log.info("Published TradeMatchRequest message[{}] to topic: {}, partition: {}, offset: {}", key, outputTopic, recordMetadata.partition(), recordMetadata.offset());
                        } else {
                            log.error("An error occurred when publishing TradeMatchRequest message[{}] to kafka topic: {}. Message: {}", key, outputTopic, avro);
                        }
                    }, cssTaskExecutor.executor());
        }
    }
}
