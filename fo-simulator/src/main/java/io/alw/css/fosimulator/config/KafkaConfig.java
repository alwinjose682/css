package io.alw.css.fosimulator.config;

import io.alw.css.serialization.trade.TradeAvro;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean("kafkaTemplateTradeMessage")
    public KafkaTemplate<String, TradeAvro> kafkaTemplate(KafkaProperties kafkaProperties) {
        var producerPropMap = kafkaProperties.buildProducerProperties(null);
        var producerFactory = new DefaultKafkaProducerFactory<String, TradeAvro>(producerPropMap);
        return new KafkaTemplate<>(producerFactory);
    }
}
