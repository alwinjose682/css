package io.alw.css.dbshared.tx.config;

import io.alw.css.dbshared.tx.TXRO;
import io.alw.css.dbshared.tx.TXRW;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/// CSS-Services use both programmatic(TXRO and TXRW) and declarative transaction. The default spring configuration use the same txManager for both programmatic and declarative apis
@Configuration
public class TxTemplateConfig {
    @Bean("txro")
    public TXRO txro(PlatformTransactionManager platformTransactionManager) {
        return new TXRO(platformTransactionManager);
    }

    @Bean("txrw")
    public TXRW txrw(PlatformTransactionManager platformTransactionManager) {
        return new TXRW(platformTransactionManager);
    }
}
