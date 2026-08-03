package io.alw.css.tradeconsumer.cashflow.model.properties;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

@ConfigurationProperties(prefix = "kafka")
public final class AppKafkaProperties {
    @NestedConfigurationProperty
    private Map<String, KafkaProperties.Producer> producer;
    @NestedConfigurationProperty
    private Map<String, KafkaProperties.Consumer> consumer;

    public Map<String, KafkaProperties.Producer> getProducer() {
        return producer;
    }

    public void setProducer(Map<String, KafkaProperties.Producer> producer) {
        this.producer = producer;
    }

    public Map<String, KafkaProperties.Consumer> getConsumer() {
        return consumer;
    }

    public void setConsumer(Map<String, KafkaProperties.Consumer> consumer) {
        this.consumer = consumer;
    }

}
