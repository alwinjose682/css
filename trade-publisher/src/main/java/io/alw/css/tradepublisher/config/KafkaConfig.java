package io.alw.css.tradepublisher.config;

import io.alw.css.serialization.confirmation.MatchStatusEventAvro;
import io.alw.css.serialization.confirmation.TradeMatchRequestAvro;
import io.alw.css.serialization.trade.TradeAvro;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean("tradeMatchRequestListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, TradeMatchRequestAvro> tradeMatchRequestListenerContainerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        DefaultKafkaConsumerFactory<String, TradeMatchRequestAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(properties);

        ConcurrentKafkaListenerContainerFactory<String, TradeMatchRequestAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactory);
        return listenerContainerFactory;
    }

    @Bean("kafkaTemplateTradeCashGenerationEvent")
    public KafkaTemplate<String, TradeAvro> kafkaTemplateTradeCashGenerationEvent(KafkaProperties kafkaProperties) {
        var producerPropMap = kafkaProperties.buildProducerProperties(null);
        var producerFactory = new DefaultKafkaProducerFactory<String, TradeAvro>(producerPropMap);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean("kafkaTemplateMatchStatusEvent")
    public KafkaTemplate<String, MatchStatusEventAvro> kafkaTemplateMatchStatusEvent(KafkaProperties kafkaProperties) {
        var producerPropMap = kafkaProperties.buildProducerProperties(null);
        var producerFactory = new DefaultKafkaProducerFactory<String, MatchStatusEventAvro>(producerPropMap);
        return new KafkaTemplate<>(producerFactory);
    }
}
