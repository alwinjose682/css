package io.alw.css.fosimulator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.alw.css.fosimulator.CssTaskExecutor;
import io.alw.css.fosimulator.TradePublisher;
import io.alw.css.fosimulator.model.properties.KafkaTopicProperties;
import io.alw.css.fosimulator.model.properties.TradeGeneratorProperties;
import io.alw.css.fosimulator.model.properties.TradeTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.tradegenerator.TradeGeneratorHandler;
import io.alw.css.serialization.trade.TradeAvro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public CssTaskExecutor cssTaskExecutor() {
        return new CssTaskExecutor();
    }

    @Bean
    public TradeGeneratorHandler tradeGeneratorHandler(TradeGeneratorProperties tradeGeneratorProperties, TradeTemplateProperties tradeTemplateProperties, TradePublisher tradePublisher, CssTaskExecutor cssTaskExecutor, RefDataService refDataService) {
        return new TradeGeneratorHandler(tradeGeneratorProperties, tradeTemplateProperties, tradePublisher, refDataService, cssTaskExecutor);
    }

    @Bean
    public TradePublisher tradePublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, TradeAvro> kafkaTemplateTradeMessage, CssTaskExecutor cssTaskExecutor) {
        return new TradePublisher(kafkaTopicProperties, kafkaTemplateTradeMessage, cssTaskExecutor);
    }
}
