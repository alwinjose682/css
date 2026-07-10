package io.alw.css.tradeconsumer.config;

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

    @Bean("tradeCashGenerationListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, TradeAvro> tradeCashGenerationListenerContainerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        DefaultKafkaConsumerFactory<String, TradeAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(properties);

        ConcurrentKafkaListenerContainerFactory<String, TradeAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactory);
        return listenerContainerFactory;
    }

    @Bean("matchStatusEventListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ConfirmationMatchStatusAvro> matchStatusEventListenerContainerFactory(KafkaProperties kafkaProperties){
        Map<String, Object> properties = kafkaProperties.buildProducerProperties(null);
        DefaultKafkaConsumerFactory<String, ConfirmationMatchStatusAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(properties);

        ConcurrentKafkaListenerContainerFactory<String, ConfirmationMatchStatusAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactory);
        return listenerContainerFactory;
    }

    @Bean("kafkaTemplateMatchRequest")
    public KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateMatchRequest(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties(null);
        DefaultKafkaProducerFactory<String, ConfirmationMatchRequestAvro> factory = new DefaultKafkaProducerFactory<>(properties);

        return new KafkaTemplate<>(factory);
    }
}
