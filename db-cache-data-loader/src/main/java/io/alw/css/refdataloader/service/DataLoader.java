package io.alw.css.refdataloader.service;

import io.alw.css.dbshared.tx.TXRO;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.common.ActionStatus;
import io.alw.css.model.referencedata.CountryCache;
import io.alw.css.model.referencedata.CurrencyCache;
import io.alw.css.model.referencedata.EntityCache;
import io.alw.css.model.referencedata.NostroCache;
import io.alw.css.refdataloader.dao.DataLoaderDao;
import io.alw.css.refdataloader.dao.model.PageRequest;
import io.alw.css.refdataloader.mapper.jpa.CountryEntityMapper;
import io.alw.css.refdataloader.mapper.jpa.CurrencyEntityMapper;
import io.alw.css.refdataloader.mapper.jpa.EntityEntityMapper;
import io.alw.css.refdataloader.mapper.jpa.NostroEntityMapper;
import io.alw.css.refdataloader.model.DataLoadType;
import io.alw.css.refdataloader.model.jpa.*;
import io.alw.css.refdtgtr.RefDataGenerator;
import io.alw.css.refdtgtr.model.GeneratedReferenceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class DataLoader {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final TXRO txro;
    private final TXRW txrw;
    private final DataLoaderDao dao;
    private final DBDataLoader dbDataLoader;
    private final SimpleCacheDataLoader cacheDataLoader;

    public DataLoader(DBDataLoader dbDataLoader, SimpleCacheDataLoader cacheDataLoader, TXRO txro, TXRW txrw, DataLoaderDao dao) {
        this.txro = txro;
        this.txrw = txrw;
        this.dao = dao;
        this.dbDataLoader = dbDataLoader;
        this.cacheDataLoader = cacheDataLoader;
    }

    /// Does the following:
    /// - Generates reference data
    /// - loads the data in DB and Cache
    /// - If data is already loaded in DB, fetches the data from DB and populate the Cache
    public void load() {
        boolean isDbDataAlreadyLoaded = txro.execute(ts -> dao.isDbDataAlreadyLoaded());
        if (isDbDataAlreadyLoaded) { // Load data from DB into Cache
            loadCacheFromDB_pagedData(); // loadCacheFromDb_wholeData_MultipleBagFetchException(); // loadCacheFromDb_lazyFetch_solution();
        } else {// Generate ref data, load into DB and cache
            generateDataAndLoadDbAndCache();
        }
    }

    /// Performs page wise data load from DB to Cache
    /// Creates a supplier, for each ReferenceDataJpaEntity type, that supplies one page at a time from DB which is used by Cache loader
    /// Note: Paged data can be retrieved correctly for only a single table. For multiple tables, a view can be created in DB
    private void loadCacheFromDB_pagedData() {
        LocalDateTime cacheDataLoadStartTime = LocalDateTime.now();
        log.info("Starting to load cache by fetching data from DB in multiple pages");
        try {
            // Create all the reference data caches
            cacheDataLoader.createCaches();

            // Fetch and load Country and Currency data from DB. Not paged as data set is small
            record CountryAndCurrency(List<CountryCache> countries, List<CurrencyCache> currencies) {
            }
            CountryAndCurrency countryAndCurrency = txro.execute(_ -> {
                List<CountryCache> countries = dao.getAllCountry().stream().map(CountryEntityMapper.instance()::entityToCache).toList();
                List<CurrencyCache> currencies = dao.getAllCurrency().stream().map(CurrencyEntityMapper.instance()::entityToCache).toList();
                return new CountryAndCurrency(countries, currencies);
            });
            cacheDataLoader.loadCountry(countryAndCurrency.countries);
            cacheDataLoader.loadCurrency(countryAndCurrency.currencies);

            // Fetch paged data for Entity, Nostro, Counterparty, Ssi, CPNettingProfile and CPSlaMapping
            final int pageSize = 200;
            // 1. Creates a supplier, for each ReferenceDataJpaEntity type, that supplies one new page at a time from DB
            // 2. Perform page wise data load from DB to Cache
            cacheDataLoader.loadDataFromDB(getPagedDataSupplier(pageSize, dao::getEntityPaged));
            cacheDataLoader.loadDataFromDB(getPagedDataSupplier(pageSize, dao::getNostroPaged));
            cacheDataLoader.loadDataFromDB(getPagedDataSupplier(pageSize, dao::getCounterpartyPaged));
            cacheDataLoader.loadDataFromDB(getPagedDataSupplier(pageSize, dao::getSsiPaged));
            cacheDataLoader.loadDataFromDB(getPagedDataSupplier(pageSize, dao::getCounterpartyNettingProfilePaged));
            cacheDataLoader.loadDataFromDB(getPagedDataSupplier(pageSize, dao::getCounterpartySlaMappingPaged));

            cacheDataLoader.releaseResources();

            log.info("Finished loading cache by fetching data from DB in pages. Time Taken: {} ms", Duration.between(cacheDataLoadStartTime, LocalDateTime.now()).toMillis());
            DataLoadStatusEntity dls = new DataLoadStatusEntity().loadType(DataLoadType.CACHE).startTime(cacheDataLoadStartTime).endTime(LocalDateTime.now()).status(ActionStatus.SUCCESS);
            txrw.executeWithoutResult(ts -> dao.save(dls));
        } catch (Exception e) {
            log.error("Exception while loading data in Cache. Exception: {}", e.getMessage());
            DataLoadStatusEntity dlsCache = new DataLoadStatusEntity().loadType(DataLoadType.CACHE).startTime(cacheDataLoadStartTime).endTime(LocalDateTime.now()).status(ActionStatus.FAILURE);
            txrw.executeWithoutResult(ts -> {
                dao.save(dlsCache);
            });
            throw e;
        }
    }

    private <T extends ReferenceDataJpaEntity> Supplier<List<T>> getPagedDataSupplier(int pageSize, Function<PageRequest, List<T>> daoMethod) {
        PageRequest pageRequest = PageRequest.first(pageSize);
        return () -> {
            List<T> page = txro.execute(_ -> daoMethod.apply(pageRequest));
            pageRequest.next();
            return page;
        };
    }

    private void generateDataAndLoadDbAndCache() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Reference data is not present in DB. Generating reference data, loading in DB and loading in cache");

        // Generate reference data
        LocalDateTime refDataGenStartTime = LocalDateTime.now();
        final GeneratedReferenceData generatedReferenceData = RefDataGenerator.generateAllReferenceData(1, 100);
        log.info("Generated reference data in {} ms", Duration.between(refDataGenStartTime, LocalDateTime.now()).toMillis());

        // Load data in DB
        LocalDateTime dbDataLoadStartTime = LocalDateTime.now();
        try {
            dbDataLoader.load(generatedReferenceData);
            LocalDateTime dbDataLoadEndTime = LocalDateTime.now();
            log.info("Finished loading the generated data in DB. Time taken: {} ms", Duration.between(dbDataLoadStartTime, dbDataLoadEndTime).toMillis());

            DataLoadStatusEntity dbLoadStatus = new DataLoadStatusEntity().loadType(DataLoadType.DB).startTime(dbDataLoadStartTime).endTime(dbDataLoadEndTime).status(ActionStatus.SUCCESS);
            txrw.executeWithoutResult(ts -> dao.save(dbLoadStatus));
        } catch (TransactionException e) {
            log.error("Exception while loading data in DB. Aborting cache load. Exception: {}", e.getMessage());
            DataLoadStatusEntity dlsDb = new DataLoadStatusEntity().loadType(DataLoadType.DB).startTime(dbDataLoadStartTime).endTime(LocalDateTime.now()).status(ActionStatus.FAILURE);
            DataLoadStatusEntity dlsCache = new DataLoadStatusEntity().loadType(DataLoadType.CACHE).startTime(dbDataLoadStartTime).endTime(LocalDateTime.now()).status(ActionStatus.ABORTED);
            txrw.executeWithoutResult(ts -> {
                dao.save(dlsDb);
                dao.save(dlsCache);
            });
            throw e;
        }

        // Load Data in cache
        LocalDateTime cacheDataLoadStartTime = LocalDateTime.now();
        log.info("Starting to load cache");
        try {
            // Create all the reference data caches
            cacheDataLoader.createCaches();

            // Load the generated reference data
            cacheDataLoader.load(generatedReferenceData);
            cacheDataLoader.releaseResources();

            LocalDateTime cacheDataLoadEndTime = LocalDateTime.now();
            log.info("Finished loading the generated data in Cache. Time taken: {} ms", Duration.between(cacheDataLoadStartTime, cacheDataLoadEndTime).toMillis());

            DataLoadStatusEntity dls = new DataLoadStatusEntity().loadType(DataLoadType.CACHE).startTime(cacheDataLoadStartTime).endTime(cacheDataLoadEndTime).status(ActionStatus.SUCCESS);
            txrw.executeWithoutResult(ts -> dao.save(dls));
        } catch (Exception e) {
            log.error("Exception while loading data in Cache. Exception: {}", e.getMessage());
            DataLoadStatusEntity dlsCache = new DataLoadStatusEntity().loadType(DataLoadType.CACHE).startTime(cacheDataLoadStartTime).endTime(LocalDateTime.now()).status(ActionStatus.FAILURE);
            txrw.executeWithoutResult(ts -> {
                dao.save(dlsCache);
            });
            throw e;
        }

        log.info("Loaded reference data in DB and cache. Total time taken: {} ms", Duration.between(startTime, LocalDateTime.now()));
    }

    /// This code is only to see the sql queries generated during lazy fetch
    /// Gets all Entity and fetch the dependent data(Nostro) for each Entity
    private void loadCacheFromDb_lazyFetch_solution() {
        log.info("NOTE: loadCacheFromDb_lazyFetch_solution is invoked. This code is only to see the sql queries generated during lazy fetch");

        record EntitiesAndNostros(List<EntityCache> entities, List<NostroCache> nostros) {
        }

        EntitiesAndNostros allRefData = txro.execute(_ -> {
            // Get All Entity and fetch the dependent data(Nostro) for each Entity
            record EntityCacheAndDependentDataCache(EntityCache entityCache, List<NostroCache> nostroCacheList) {
            }
            List<EntityCache> entities = new ArrayList<>();
            List<NostroCache> nostros = new ArrayList<>();
            List<EntityCacheAndDependentDataCache> edList = dao
                    .getAllEntity()
                    .stream()
                    .<EntityCacheAndDependentDataCache>mapMulti((entity, entityAndDependentDataConsumer) -> {
                        List<NostroCache> nostroCacheList = entity.getNostros().stream().map(NostroEntityMapper.instance()::entityToCache).toList();
                        EntityCache entityCache = EntityEntityMapper.instance().entityToCache(entity);
                        entityAndDependentDataConsumer.accept(new EntityCacheAndDependentDataCache(entityCache, nostroCacheList));
                    })
                    .toList();

            for (EntityCacheAndDependentDataCache ed : edList) {
                entities.add(ed.entityCache);
                nostros.addAll(ed.nostroCacheList);
            }

            return new EntitiesAndNostros(entities, nostros);
        });

        String entityCodes = allRefData.entities.stream().map(EntityCache::entityCode).collect(Collectors.joining(","));
        String nostroIds = allRefData.nostros.stream().map(NostroCache::nostroID).collect(Collectors.joining(","));

        log.info("""
                EntityCodes: {}
                NostroIds: {}
                """, entityCodes, nostroIds);

        // This code is only to see the sql queries generated during lazy fetch
    }

    /// This code is only to see the sql query and the MultipleBagFetchException generated by hibernate
    /// Attempts to retrieve Counterparty and all its associations (Ssi, CPNettingProfile, CPSlaMapping) in a single sql query with a huge cartesian product
    /// This data retrieval does NOT result in N+1 issue, but the issue is huge data set due to huge cartesian product and fetching the huge data in a single transaction
    private void loadCacheFromDb_wholeData_MultipleBagFetchException() {
        log.info("NOTE: loadCacheFromDb_wholeData_MultipleBagFetchException is invoked. This code is only to see the sql query and the MultipleBagFetchException generated by hibernate");

        List<CounterpartyEntity> cpEntities = txro.execute(_ -> dao.getCounterpartyAndDependentData());

        cpEntities.forEach(cp -> {
            String cpCode = cp.getCounterpartyEntityPK().counterpartyCode();
            String ssis = cp.getSsiList().stream().map(ssi -> ssi.getSsiEntityPK().ssiID()).collect(Collectors.joining(","));
            String nps = cp.getCpNettingProfiles().stream().map(np -> String.valueOf(np.getCounterpartyNettingProfileEntityPK().nettingProfileID())).collect(Collectors.joining(","));
            String sms = cp.getSlaMappingEntities().stream().map(sm -> String.valueOf(sm.getCounterpartySlaMappingEntityPK().mappingID())).collect(Collectors.joining(","));
            log.info("""
                    cp code   : {}
                    cp ssis   : {}
                    cp NetProf: {}
                    cp SlaMap : {}
                    """, cpCode, ssis, nps, sms);
        });

        // This code is only to see the sql query generated by hibernate
    }
}
