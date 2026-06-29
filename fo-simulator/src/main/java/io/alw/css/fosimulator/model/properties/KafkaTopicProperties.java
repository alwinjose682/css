package io.alw.css.fosimulator.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("app.kafka.topic")
public class KafkaTopicProperties {
    private final String tradeOutput;

    @ConstructorBinding
    public KafkaTopicProperties(String tradeOutput) {
        this.tradeOutput = tradeOutput;
    }

    public String tradeOutputTopic() {
        return tradeOutput;
    }
}
