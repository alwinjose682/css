package io.alw.css.tradeconsumer.confirmation.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    private final String tradeMatchRequest;
    private final String tradeMatchStatusEvent;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeMatchRequest, String tradeMatchStatusEvent) {
        this.tradeMatchRequest = tradeMatchRequest;
        this.tradeMatchStatusEvent = tradeMatchStatusEvent;
    }

    public String tradeMatchRequestTopic() {
        return tradeMatchRequest;
    }

    public String tradeMatchStatusEvent() {
        return tradeMatchStatusEvent;
    }
}