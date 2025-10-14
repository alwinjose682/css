package io.alw.css.refdataloader.service;

import io.alw.css.dbshared.tx.TXRO;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.referencedata.*;
import io.alw.css.refdataloader.dao.DataLoaderDao;
import io.alw.css.refdtgtr.model.CounterpartyAndDependentData;
import io.alw.css.refdtgtr.model.EntityAndDependentData;
import io.alw.css.refdtgtr.model.GeneratedReferenceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DBDataLoader {
    private final static Logger log = LoggerFactory.getLogger(DBDataLoader.class);
    private final DataLoaderDao dao;
    private final TXRO txro;
    private final TXRW txrw;

    public DBDataLoader(DataLoaderDao dao, TXRO txro, TXRW txrw) {
        this.dao = dao;
        this.txro = txro;
        this.txrw = txrw;
    }

    /// Does the following:
    ///
    /// - Load Country:
    /// Whole country data is persisted in a SINGLE transaction
    /// - Load Currency:
    /// Whole currency data is persisted in a SINGLE transaction
    /// - Load Entity and dependent data:
    /// Each entity and its corresponding dependent data is persisted in SEPARATE transactions.
    /// The whole dependent data is usually small enough to fit in a single transaction without timing out
    /// - Load Counterparty and dependent data:
    /// Each counterparty and its corresponding dependent data is persisted in SEPARATE transactions.
    /// The whole dependent data is usually small enough to fit in a single transaction without timing out
    public void load(GeneratedReferenceData generatedReferenceData) {
        log.info("Staring DB data load");
        LocalDateTime dbLoadStartTime = LocalDateTime.now();

        // Load Country
        // Whole country data is persisted in a SINGLE transaction
        List<Country> countries = generatedReferenceData.countries();
        txrw.executeWithoutResult(ts -> {
            for (Country country : countries) {
                dao.save(country);
            }
        });
        log.info("Loaded data: country, count: {}", countries.size());


        // Load Currency
        // Whole currency data is persisted in a SINGLE transaction
        List<Currency> currencies = generatedReferenceData.currencies();
        txrw.executeWithoutResult(ts -> {
            for (Currency currency : currencies) {
                dao.save(currency);
            }
        });
        log.info("Loaded data: currency, count: {}", currencies.size());

        // Load Entity and dependent data
        // Each entity and its corresponding dependent data is persisted in SEPARATE transactions
        // The whole dependent data is usually small enough to fit in a single transaction without timing out
        List<EntityAndDependentData> entityAndDependentData = generatedReferenceData.entityAndDependentData();
        int entityCount = 0, nostroCount = 0;
        for (EntityAndDependentData entityAndDependentDatum : entityAndDependentData) {
            Entity entity = entityAndDependentDatum.entity();
            List<Nostro> nostros = entityAndDependentDatum.nostros();
            entityCount += 1;
            nostroCount += nostros.size();

            txrw.executeWithoutResult("entityCode: " + entity.entityCode(), _ -> {
                dao.save(entity, nostros);
            });
        }
        log.info("Loaded data: entity[{}] and nostro[{}]", entityCount, nostroCount);

        // Load Counterparty and dependent data
        // Each counterparty and its corresponding dependent data is persisted in SEPARATE transactions
        // The whole dependent data is usually small enough to fit in a single transaction without timing out
        List<CounterpartyAndDependentData> counterpartyAndDependentData = generatedReferenceData.counterpartyAndDependentData();
        int cpCount = 0, ssiCount = 0, nettingProfileCount = 0, slaMappingCount = 0;
        for (CounterpartyAndDependentData counterpartyAndDependentDatum : counterpartyAndDependentData) {
            Counterparty counterparty = counterpartyAndDependentDatum.counterparty();
            List<Ssi> ssis = counterpartyAndDependentDatum.ssis();
            List<CounterpartyNettingProfile> nettingProfiles = counterpartyAndDependentDatum.counterpartyNettingProfiles();
            List<CounterpartySlaMapping> slaMappings = counterpartyAndDependentDatum.counterpartySlaMappings();
            cpCount += 1;
            ssiCount += ssis.size();
            nettingProfileCount += nettingProfiles.size();
            slaMappingCount += slaMappings.size();

            txrw.executeWithoutResult("counterpartyID: " + counterparty.counterpartyCode(), _ -> {
                dao.save(counterparty, ssis, nettingProfiles, slaMappings);
            });
        }
        log.info("Loaded data: counterparty[{}], ssi[{}], nettingProfile[{}], slaMapping[{}]", cpCount, ssiCount, nettingProfileCount, slaMappingCount);

        log.info("Finished DB data load in {} ms", Duration.between(dbLoadStartTime, LocalDateTime.now()).toMillis());
    }
}
