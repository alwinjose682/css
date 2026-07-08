package io.alw.css.tradepublisher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    // These are not named as *Topic because @ConstructorBinding is used and when naming a kafka topic it does not make sense to suffix 'Topic' for every topic
    private final String tradeCashflowEvent;
    private final String tradeMatchStatusEvent;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeCashflowEvent, String tradeMatchStatusEvent) {
        this.tradeCashflowEvent = tradeCashflowEvent;
        this.tradeMatchStatusEvent = tradeMatchStatusEvent;
    }

    public String tradeOutputTopic() {
        return tradeCashflowEvent;
    }

    public String tradeMatchStatusEvent() {
        return tradeMatchStatusEvent;
    }
}
