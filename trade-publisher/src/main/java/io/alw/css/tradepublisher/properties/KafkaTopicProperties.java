package io.alw.css.tradepublisher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    // These are not named as *Topic because @ConstructorBinding is used and when naming a kafka topic it does not make sense to suffix 'Topic' for every topic
    private final String tradeCashGenerationEvent;
    private final String confirmationMatchEvent;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeCashGenerationEvent, String confirmationMatchEvent) {
        this.tradeCashGenerationEvent = tradeCashGenerationEvent;
        this.confirmationMatchEvent = confirmationMatchEvent;
    }

    public String tradeOutputTopic() {
        return tradeCashGenerationEvent;
    }

    public String confirmationMatchEventTopic() {
        return confirmationMatchEvent;
    }
}
