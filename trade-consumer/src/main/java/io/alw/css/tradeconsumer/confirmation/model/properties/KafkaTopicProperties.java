package io.alw.css.tradeconsumer.confirmation.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    private final String tradeMatchRequest;
    private final String confirmationMatchEvent;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeMatchRequest, String confirmationMatchEvent) {
        this.tradeMatchRequest = tradeMatchRequest;
        this.confirmationMatchEvent = confirmationMatchEvent;
    }

    public String tradeMatchRequestTopic() {
        return tradeMatchRequest;
    }

    public String confirmationMatchEventTopic() {
        return confirmationMatchEvent;
    }
}