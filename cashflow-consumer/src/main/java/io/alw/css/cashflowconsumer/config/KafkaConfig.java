package io.alw.css.cashflowconsumer.config;

import io.alw.css.serialization.trade.TradeAvro;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TradeAvro> foCashMessageListenerContainerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties(null);
        DefaultKafkaConsumerFactory<String, TradeAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);

        ConcurrentKafkaListenerContainerFactory<String, TradeAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactory);
        return listenerContainerFactory;
    }
}
