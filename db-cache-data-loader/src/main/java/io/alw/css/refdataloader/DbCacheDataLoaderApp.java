package io.alw.css.refdataloader;

import io.alw.css.refdataloader.service.DataLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;

@SpringBootApplication(scanBasePackages = {"io.alw.css.refdataloader", "io.alw.css.dbshared.tx.config"})
@ConfigurationPropertiesScan("io.alw.css.refdataloader.model.properties")
public class DbCacheDataLoaderApp {
    private final DataLoader dataLoader;

    public DbCacheDataLoaderApp(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public static void main(String[] args) {
        SpringApplication.run(DbCacheDataLoaderApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startLoadingData() throws InterruptedException {
        dataLoader.load();
    }

    @EventListener(org.springframework.context.event.ContextStoppedEvent.class)
    public void applicationContextStopEvent() {
        System.out.println("Context stopped");
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void applicationContextCloseEvent() {
        System.out.println("Context closed");
    }
}

/*
SELECT * FROM country;
SELECT * FROM currency;
SELECT * FROM entity;
SELECT * FROM nostro;
SELECT * FROM counterparty;
SELECT * FROM ssi;
SELECT * FROM COUNTERPARTY_NETTING_PROFILE;
SELECT * FROM COUNTERPARTY_SLA_MAPPING;

TRUNCATE TABLE country;
TRUNCATE TABLE currency;
TRUNCATE TABLE entity;
TRUNCATE TABLE nostro;
TRUNCATE TABLE counterparty;
TRUNCATE TABLE ssi;
TRUNCATE TABLE COUNTERPARTY_NETTING_PROFILE;
TRUNCATE TABLE COUNTERPARTY_SLA_MAPPING;
 */