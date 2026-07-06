package io.alw.css.tradeconsumer.confirmation.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    private final String tradeMatchRequest;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeMatchRequest) {
        this.tradeMatchRequest = tradeMatchRequest;
    }

    public String tradeMatchRequestTopic() {
        return tradeMatchRequest;
    }
}