package io.alw.css.tradeconsumer.confirmation.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    private final String tradeMatchRequest;
    private final String confirmationMatchStatus;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeMatchRequest, String confirmationMatchStatus) {
        this.tradeMatchRequest = tradeMatchRequest;
        this.confirmationMatchStatus = confirmationMatchStatus;
    }

    public String tradeMatchRequestTopic() {
        return tradeMatchRequest;
    }

    public String confirmationMatchStatusTopic() {
        return confirmationMatchStatus;
    }
}