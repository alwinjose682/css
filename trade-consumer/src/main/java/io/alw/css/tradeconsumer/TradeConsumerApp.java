package io.alw.css.tradeconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// TODO: jdbc batching, disable auto-commit, disable open-session-in-view. Must be done one at a time
// TODO: MDC logging
@SpringBootApplication(scanBasePackages = "io.alw.css.tradeconsumer")
@ConfigurationPropertiesScan(value = {"io.alw.css.tradeconsumer.cashflow.model.properties"})
public class TradeConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(TradeConsumerApp.class, args);
    }
}
