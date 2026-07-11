package io.alw.css.tradeconsumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.alw.css.dbshared.tx.TXRO;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradeconsumer.CssTaskExecutor;
import io.alw.css.tradeconsumer.cashflow.model.properties.SuppressionConfig;
import io.alw.css.tradeconsumer.cashflow.repository.CashflowRejectionRepository;
import io.alw.css.tradeconsumer.cashflow.repository.CashflowRepository;
import io.alw.css.tradeconsumer.cashflow.repository.CashflowStore;
import io.alw.css.tradeconsumer.cashflow.repository.TradeLinkRepository;
import io.alw.css.tradeconsumer.cashflow.service.CashflowEnrichmentService;
import io.alw.css.tradeconsumer.cashflow.service.CashflowVersionService;
import io.alw.css.tradeconsumer.confirmation.ConfirmationMatchRequestPublisher;
import io.alw.css.tradeconsumer.confirmation.model.properties.KafkaTopicProperties;
import io.alw.css.tradeconsumer.service.CacheService;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages = "io.alw.css.tradeconsumer.repository")
@EntityScan(basePackages = "io.alw.css.tradeconsumer.cashflow.jpa")
// no @EnableTransactionManagement. Declarative tx is not used. Programmatic tx is used instead
public class AppConfig {

    @Bean("cashflowIdSeqIncrementer")
    public DataFieldMaxValueIncrementer cashflowIdSeqIncrementer(DataSource dataSource){
        return new OracleSequenceMaxValueIncrementer(dataSource, "cashflow_seq");
        // NOTE: Spring does not cache result produced by a sequence. But it does for ids generated via other means like table
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    // Explicitly making TXRW and TXRO as spring beans.
    // 'spring.factories' is defined for auto configuration. It does not work for 'trade-consumer', but works for 'db-cache-data-loader' where spring-data-jpa is not used
    // Do not know the reason
    @Bean("txro")
    public TXRO txro(PlatformTransactionManager platformTransactionManager) {
        return new TXRO(platformTransactionManager);
    }

    @Bean("txrw")
    public TXRW txrw(PlatformTransactionManager platformTransactionManager) {
        return new TXRW(platformTransactionManager);
    }

    @Bean
    public ApplicationStartupEvent applicationStartupEvent(RestTemplate restTemplate, CashflowRepository cashflowRepository, ObjectMapper objectMapper) {
        return new ApplicationStartupEvent(restTemplate, cashflowRepository, objectMapper);
    }

    @Bean
    public CacheService cacheService(ClientConfiguration clientConfiguration) {
        return new CacheService(clientConfiguration);
    }

    @Bean
    public CashflowVersionService cashflowVersionManager(CashflowStore cashflowStore, TXRW txrw, TXRO txro) {
        return new CashflowVersionService(cashflowStore, txrw, txro);
    }

    @Bean
    public CashflowEnrichmentService cashflowEnricher(SuppressionConfig suppressionConfig, CacheService cacheService) {
        return new CashflowEnrichmentService(suppressionConfig, cacheService);
    }

    @Bean
    public CashflowStore cashflowStore(CashflowRepository cashflowRepository, CashflowRejectionRepository cashflowRejectionRepository, TradeLinkRepository tradeLinkRepository, DataFieldMaxValueIncrementer cashflowIdSeqIncrementer) {
        return new CashflowStore(cashflowRepository, cashflowRejectionRepository, tradeLinkRepository, cashflowIdSeqIncrementer);
    }

    @Bean
    public CssTaskExecutor cssTaskExecutor() {
        return new CssTaskExecutor();
    }

    @Bean
    public ConfirmationMatchRequestPublisher tradeMatchRequestPublisher(KafkaTopicProperties kafkaTopicProperties, KafkaTemplate<String, ConfirmationMatchRequestAvro> kafkaTemplateConfMatchRequest, CssTaskExecutor cssTaskExecutor) {
        return new ConfirmationMatchRequestPublisher(kafkaTopicProperties, kafkaTemplateConfMatchRequest, cssTaskExecutor);
    }
}
