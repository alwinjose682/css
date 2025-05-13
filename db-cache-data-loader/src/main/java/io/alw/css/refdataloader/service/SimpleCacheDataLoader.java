package io.alw.css.refdataloader.service;

import io.alw.css.model.referencedata.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public final class SimpleCacheDataLoader extends CacheDataLoader {
    private static final Logger log = LoggerFactory.getLogger(SimpleCacheDataLoader.class);

    private final ExecutorService vt;
    private final ClientConfiguration thinClientConfiguration;
    private IgniteClient igniteClient;

    private ClientCache<CounterpartyCache.Key, CounterpartyCache> counterpartyCache;
    private ClientCache<CounterpartySlaMappingCache.Key, CounterpartySlaMappingCache> cpSlaMappingCache;
    private ClientCache<CounterpartyNettingProfileCache.Key, CounterpartyNettingProfileCache> cpNettingProfileCache;
    private ClientCache<SsiCache.Key, SsiCache> ssiCache;
    private ClientCache<EntityCache.Key, EntityCache> entityCache;
    private ClientCache<NostroCache.Key, NostroCache> nostroCache;
    private ClientCache<CountryCache.Key, CountryCache> countryCache;
    private ClientCache<CurrencyCache.Key, CurrencyCache> currencyCache;

    public SimpleCacheDataLoader(ClientConfiguration thinClientConfiguration) {
        this.vt = Executors.newVirtualThreadPerTaskExecutor();
        this.thinClientConfiguration = thinClientConfiguration;
    }

    @PostConstruct
    @Override
    protected void initIgniteClient() {
        igniteClient = Ignition.startClient(thinClientConfiguration);
    }

    @PreDestroy
    @Override
    public void releaseResources() {
        try {
            igniteClient.close();
        } catch (Exception e) {
            // Ignored
        }
    }

    /// Creates the caches from thin client
    @Override
    void createCaches() {
        ClientCacheConfiguration tempCacheConfig = new ClientCacheConfiguration();
        tempCacheConfig.setCacheMode(CacheMode.REPLICATED);
//        tempCacheConfig.setBackups(1); // TODO backups=2147483647, even when backup is set as 0 and 1
        tempCacheConfig.setName("tempCache");
        tempCacheConfig.setSqlSchema("PUBLIC");
        // This cache is created only to run query method to create cache using sql ddl
        ClientCache<Object, Object> cache = igniteClient.getOrCreateCache(tempCacheConfig);

        // Create reference data caches and indexes via sql CREATE statement
        getCacheTableDdlQueryObjs().forEach(sfq -> cache.query(sfq).getAll());

        // Get cache instances
        counterpartyCache = igniteClient.getOrCreateCache(IgniteCacheName.COUNTERPARTY);
        cpSlaMappingCache = igniteClient.getOrCreateCache(IgniteCacheName.CP_SLA_MAPPING);
        cpNettingProfileCache = igniteClient.getOrCreateCache(IgniteCacheName.CP_NETTING_PROFILE);
        ssiCache = igniteClient.getOrCreateCache(IgniteCacheName.SSI);
        entityCache = igniteClient.getOrCreateCache(IgniteCacheName.ENTITY);
        nostroCache = igniteClient.getOrCreateCache(IgniteCacheName.NOSTRO);
        countryCache = igniteClient.getOrCreateCache(IgniteCacheName.COUNTRY);
        currencyCache = igniteClient.getOrCreateCache(IgniteCacheName.CURRENCY);

        // Destroy the temporary cache
        igniteClient.destroyCache("tempCache");
    }

    /// Loads currency via key-value API and fetches the same data from cache using both Key-Value and SQL APIs
    // TODO: Tests can be written, similar to this method, to make sure that the cache operations can be done via both key-value and SQL APIs
    @Override
    void loadCurrency(List<CurrencyCache> currencies) {

        /*

        NOTE:
            There is an intricacy when inserting via SQL API.
            If '_key' is not specified in insert statement, we will get null value for key values when fetching data via key-value API
            To solve this, we must explicitly specify '_key' in insert statement and further, for the insert statement to work following property needs to be set to true which can have some implications that is not clear. No official documentation found
            System.setProperty(IgniteSystemProperties.IGNITE_SQL_ALLOW_KEY_VAL_UPDATES, "true");

        //        SqlFieldsQuery qry = new SqlFieldsQuery("""
        //                INSERT INTO currency
        //                (_key, currCode, countryCode, pmFlag, cutOffTime, active, entryTime)
        //                VALUES
        //                (?, ?, ?, ?, ?, ?, ?)
        //                """);

        SqlFieldsQuery qry = new SqlFieldsQuery("""
                INSERT INTO currency
                (currCode, countryCode, pmFlag, cutOffTime, active, entryTime)
                VALUES
                (?, ?, ?, ?, ?, ?)
                """);

        // Insert first 5 currencies via key-value and SQL API. These will be fetched via both APIs to make sure that the cache configuration is correct and it works with both APIs
        final var curr = currencies.get(0);
        final var curr1 = currencies.get(1);
        final var curr2 = currencies.get(2);
        final var curr3 = currencies.get(3);
        final var curr4 = currencies.get(4);

//        currencyCache.query(qry.setArgs(new AffinityKey<>(curr.getCurrCode()), curr.getCurrCode(), curr.getCountryCode(), curr.isPmFlag(), curr.getCutOffTime(), curr.isActive(), curr.getEntryTime())).getAll();
        currencyCache.query(qry.setArgs(curr.getCurrCode(), curr.getCountryCode(), curr.isPmFlag(), curr.getCutOffTime(), curr.isActive(), curr.getEntryTime())).getAll();
        currencyCache.put(new CurrencyCache.Key(curr1.getCurrCode()), curr1);
        currencyCache.query(qry.setArgs(curr2.getCurrCode(), curr2.getCountryCode(), curr2.isPmFlag(), curr2.getCutOffTime(), curr2.isActive(), curr2.getEntryTime())).getAll();
        currencyCache.put(new CurrencyCache.Key(curr3.getCurrCode()), curr3);
        currencyCache.query(qry.setArgs(curr4.getCurrCode(), curr4.getCountryCode(), curr4.isPmFlag(), curr4.getCutOffTime(), curr4.isActive(), curr4.getEntryTime())).getAll();

        // Load rest of the currencies, if any, to the cache
        for (int idx = 5; idx < currencies.size(); ++idx) {
            CurrencyCache c = currencies.get(idx);
            currencyCache.put(new CurrencyCache.Key(c.getCurrCode()), c);
        }
        log.debug("Successfully loaded currencies to cache via both Ignite key-value and SQL APIs");

        */

        currencies.forEach(curr -> currencyCache.put(new CurrencyCache.Key(curr.currCode()), curr));

        // Fetch all the currencies via both key-value and SQL APIs
        currencyCache
                .query(new ScanQuery<CurrencyCache.Key, CurrencyCache>())
                .getAll()
                .forEach(entry -> {
                    var key = entry.getKey();
                    var value = entry.getValue();
                    log.debug("Currency from cache using key-value api: {}: {}, {}, {}, {}, {}", key.getCurrCode(), value.getCurrCode(), value.getCountryCode(), value.getCutOffTime(), value.isActive(), value.getEntryTime());
                });

        currencyCache
                .query(new SqlFieldsQuery("select c.currCode, c.countryCode, c.cutOffTime, c.active, c.entryTime from currency c"))
                .getAll()
                .forEach(ol -> {
                    var currCode = (String) ol.get(0);
                    var countryCode = (String) ol.get(1);
                    var cutOffTime = ((Time) ol.get(2)).toLocalTime();
                    var active = (boolean) ol.get(3);
                    var entryTime = ((Timestamp) ol.get(4)).toLocalDateTime();

                    log.debug("Currency from cache using SQL api: {}, {}, {}, {}, {}", currCode, countryCode, cutOffTime, active, entryTime);
                });

        log.info("Loaded currencies[{}] to cache", currencies.size());
    }

    @Override
    void loadCounterparty(List<CounterpartyCache> counterparties) {
        counterparties.forEach(rd -> counterpartyCache.put(new CounterpartyCache.Key(rd.counterpartyCode()), rd));
        log.info("Loaded counterparties[{}] to cache", counterparties.size());
    }

    @Override
    void loadCPSlaMapping(List<CounterpartySlaMappingCache> cpsms) {
        cpsms.forEach(rd -> cpSlaMappingCache.put(new CounterpartySlaMappingCache.Key(rd.mappingID(), rd.getCounterpartyCode()), rd));
        log.info("Loaded cpSlaMappings[{}] to cache", cpsms.size());
    }

    @Override
    void loadCPNettingProfile(List<CounterpartyNettingProfileCache> cpnps) {
        cpnps.forEach(rd -> cpNettingProfileCache.put(new CounterpartyNettingProfileCache.Key(rd.nettingProfileID(), rd.getCounterpartyCode()), rd));
        log.info("Loaded cpNettingProfiles[{}] to cache", cpnps.size());
    }

    @Override
    void loadSsi(List<SsiCache> ssis) {
        ssis.forEach(rd -> ssiCache.put(new SsiCache.Key(rd.ssiID(), rd.getCounterpartyCode()), rd));
        log.info("Loaded ssis[{}] to cache", ssis.size());
    }

    @Override
    void loadEntity(List<EntityCache> entities) {
        entities.forEach(rd -> entityCache.put(new EntityCache.Key(rd.entityCode()), rd));
        log.info("Loaded entities[{}] to cache", entities.size());
    }

    @Override
    void loadNostro(List<NostroCache> nostros) {
        nostros.forEach(rd -> nostroCache.put(new NostroCache.Key(rd.nostroID(), rd.getEntityCode()), rd));
        log.info("Loaded nostros[{}] to cache", nostros.size());
    }

    @Override
    void loadCountry(List<CountryCache> countries) {
        countries.forEach(cntry -> countryCache.put(new CountryCache.Key(cntry.countryCode()), cntry));
        log.info("Loaded countries[{}] to cache", countries.size());
    }
}
