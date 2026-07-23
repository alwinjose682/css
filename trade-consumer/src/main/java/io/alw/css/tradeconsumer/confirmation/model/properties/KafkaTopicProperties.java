package io.alw.css.tradeconsumer.confirmation.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    private final String confirmationMatchRequest;
    private final String confirmationMatchEvent;

    @ConstructorBinding
    public KafkaTopicProperties(String confirmationMatchRequest, String confirmationMatchEvent) {
        this.confirmationMatchRequest = confirmationMatchRequest;
        this.confirmationMatchEvent = confirmationMatchEvent;
    }

    public String confirmationMatchRequestTopic() {
        return confirmationMatchRequest;
    }

    public String confirmationMatchEventTopic() {
        return confirmationMatchEvent;
    }
}