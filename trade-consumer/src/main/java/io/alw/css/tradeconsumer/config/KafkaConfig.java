package io.alw.css.tradeconsumer.config;

import io.alw.css.serialization.confirmation.ConfirmationMatchEventAvro;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.tradeconsumer.cashflow.model.properties.AppKafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableKafka
public class KafkaConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Autowired
    AppKafkaProperties appKafkaProperties;

    @Autowired
    KafkaProperties kafkaProperties;

    @Bean("tradeListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, TradeAvro> tradeCashGenerationListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        System.out.println("----------------------------------- TradeAvro");
        properties.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEach(e -> System.out.println("Key: " + e.getKey() + ", Val: " + e.getValue()));

        DefaultKafkaConsumerFactory<String, TradeAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(buildKafkaProperties("tradecash", null));
        ConcurrentKafkaListenerContainerFactory<String, TradeAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(
                (ConcurrentKafkaListenerContainerFactory<Object, Object>) ((Object) listenerContainerFactory),
                (DefaultKafkaConsumerFactory<Object, Object>) ((Object) consumerFactory)
        );

        return listenerContainerFactory;
    }

    @Bean("confListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ConfirmationMatchEventAvro> confMatchEventListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        System.out.println("----------------------------------- ConfirmationMatchEventAvro");
        properties.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEach(e -> System.out.println("Key: " + e.getKey() + ", Val: " + e.getValue()));

        DefaultKafkaConsumerFactory<String, ConfirmationMatchEventAvro> consumerFactory = new DefaultKafkaConsumerFactory<>(buildKafkaProperties("confmatch", null));
        ConcurrentKafkaListenerContainerFactory<String, ConfirmationMatchEventAvro> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(
                (ConcurrentKafkaListenerContainerFactory<Object, Object>) ((Object) listenerContainerFactory),
                (DefaultKafkaConsumerFactory<Object, Object>) ((Object) consumerFactory)
        );

        return listenerContainerFactory;
    }

    @Bean("kafkaTemplateConfMatchRequest")
    public KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateConfMatchRequest() {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties(null);
        DefaultKafkaProducerFactory<String, ConfirmationMatchRequestAvro> factory = new DefaultKafkaProducerFactory<>(properties);

        return new KafkaTemplate<>(factory);
    }

    // Builds the consumer specific factory
    private Map<String, Object> buildKafkaProperties(String consumerName, SslBundles sslBundles) {
        Map<String, Object> properties = buildCommonKafkaProperties(sslBundles);

        if (Objects.nonNull(appKafkaProperties.getConsumer())) {
            KafkaProperties.Consumer consumerProperties = appKafkaProperties.getConsumer().get(consumerName);
            if (Objects.nonNull(consumerProperties)) {
                properties.putAll(consumerProperties.buildProperties(sslBundles));
            }
        }

        log.info("Kafka Consumer '{}' properties: {}", consumerName, properties);
        return properties;
    }

    ///  Builds kafka common properties from spring's [KafkaProperties]
    private Map<String, Object> buildCommonKafkaProperties(SslBundles sslBundles) {
        Map<String, Object> properties = new HashMap<>();

        // The below is copied from: kafkaProperties.buildCommonProperties(); This method is private
        if (kafkaProperties.getBootstrapServers() != null) {
            properties.put("bootstrap.servers", kafkaProperties.getBootstrapServers());
        }
        if (kafkaProperties.getClientId() != null) {
            properties.put("client.id", kafkaProperties.getClientId());
        }
        properties.putAll(kafkaProperties.getSsl().buildProperties(sslBundles));
        properties.putAll(kafkaProperties.getSecurity().buildProperties());
        if (!CollectionUtils.isEmpty(kafkaProperties.getProperties())) {
            properties.putAll(kafkaProperties.getProperties());
        }

        return properties;
    }
}
