package io.alw.css.tradepublisher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.alw.css.serialization.confirmation.ConfirmationMatchEventAvro;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.tradepublisher.CssTaskExecutor;
import io.alw.css.tradepublisher.confirmation.ConfirmationMatchEventPublisher;
import io.alw.css.tradepublisher.generator.GeneratorHandler;
import io.alw.css.tradepublisher.properties.ConfirmationMatchEventGeneratorProperties;
import io.alw.css.tradepublisher.properties.KafkaTopicProperties;
import io.alw.css.tradepublisher.properties.TradeGeneratorProperties;
import io.alw.css.tradepublisher.properties.TradeTemplateProperties;
import io.alw.css.tradepublisher.trade.TradePublisher;
import io.alw.css.tradepublisher.trade.service.RefDataService;
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
    public GeneratorHandler generatorHandler(TradeGeneratorProperties tradeGeneratorProperties, ConfirmationMatchEventGeneratorProperties confirmationMatchEventGeneratorProperties, TradeTemplateProperties tradeTemplateProperties, TradePublisher tradePublisher, ConfirmationMatchEventPublisher confirmationMatchEventPublisher, RefDataService refDataService, CssTaskExecutor cssTaskExecutor) {
        return new GeneratorHandler(tradeGeneratorProperties, confirmationMatchEventGeneratorProperties, tradeTemplateProperties, tradePublisher, confirmationMatchEventPublisher, refDataService, cssTaskExecutor);
    }

    @Bean
    public TradePublisher tradePublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, TradeAvro> kafkaTemplateTradeCashGenerationEvent, CssTaskExecutor cssTaskExecutor) {
        return new TradePublisher(kafkaTopicProperties, kafkaTemplateTradeCashGenerationEvent, cssTaskExecutor);
    }

    @Bean
    public ConfirmationMatchEventPublisher confirmationMatchEventPublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, ConfirmationMatchEventAvro> kafkaTemplateConfMatchEvent, CssTaskExecutor cssTaskExecutor) {
        return new ConfirmationMatchEventPublisher(kafkaTopicProperties, kafkaTemplateConfMatchEvent, cssTaskExecutor);
    }
}
