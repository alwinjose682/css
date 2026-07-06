package io.alw.css.tradepublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "io.alw.css.tradepublisher")
@ConfigurationPropertiesScan("io.alw.css.tradepublisher.trade.model.properties")
public class TradePublisherApp {
    public static void main(String[] args) {
        SpringApplication.run(TradePublisherApp.class, args);
    }
}
