package io.alw.css.tradepublisher.config;

import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.serialization.confirmation.ConfirmationMatchStatusAvro;
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
    public ConcurrentKafkaListenerContainerFactory<String, ConfirmationMatchRequestAvro> tradeMatchRequestListenerContainerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        DefaultKafkaConsumerFactory<String, ConfirmationMatchRequestAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(properties);

        ConcurrentKafkaListenerContainerFactory<String, ConfirmationMatchRequestAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
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
    public KafkaTemplate<String, ConfirmationMatchStatusAvro> kafkaTemplateMatchStatusEvent(KafkaProperties kafkaProperties) {
        var producerPropMap = kafkaProperties.buildProducerProperties(null);
        var producerFactory = new DefaultKafkaProducerFactory<String, ConfirmationMatchStatusAvro>(producerPropMap);
        return new KafkaTemplate<>(producerFactory);
    }
}
