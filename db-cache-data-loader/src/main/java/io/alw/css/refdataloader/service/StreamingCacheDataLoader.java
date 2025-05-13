package io.alw.css.refdataloader.service;

import io.alw.css.model.referencedata.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@Service -> Does NOT work as of now!!!
public final class StreamingCacheDataLoader extends CacheDataLoader {
    private static final Logger log = LoggerFactory.getLogger(StreamingCacheDataLoader.class);

    private final ExecutorService vt;
    private final IgniteConfiguration thickClientConfiguration;
    private Ignite ignite;
    private CacheDataStreamerObjects dataStreamers;

    private IgniteCache<EntityCache.Key, EntityCache> entityCache;
    private IgniteCache<CountryCache.Key, CountryCache> countryCache;
    private IgniteCache<CurrencyCache.Key, CurrencyCache> currencyCache;

    public StreamingCacheDataLoader(IgniteConfiguration thickClientConfiguration) {
        this.vt = Executors.newVirtualThreadPerTaskExecutor();
        this.thickClientConfiguration = thickClientConfiguration;
    }

    @PostConstruct
    @Override
    protected void initIgniteClient() {
        this.ignite = Ignition.start(thickClientConfiguration);
    }

    @PreDestroy
    @Override
    public void releaseResources() {
        try {
            dataStreamers.closeAllStreamers(); // closing data streamer will flush pending data if any
            vt.close();
            ignite.close();
        } catch (Exception e) {
            // Ignored
        }
    }

    /// Creates the caches from thick client
    @Override
    void createCaches() {
        CacheConfiguration<String, String> tempCacheConfig = new CacheConfiguration<>();
        tempCacheConfig.setCacheMode(CacheMode.REPLICATED);
//        tempCacheConfig.setBackups(1); // TODO backups=2147483647, even when backup is set as 0 and 1
        tempCacheConfig.setName("tempCache");
        tempCacheConfig.setSqlSchema("PUBLIC");
        // This cache is created only to run query method to create cache using sql ddl
        IgniteCache<String, String> cache = ignite.getOrCreateCache(tempCacheConfig);

        // Create reference data caches and indexes via sql CREATE statement
        getCacheTableDdlQueryObjs().forEach(sfq -> cache.query(sfq).getAll());
        log.info("Created caches via sql api");

        // Get data streamers for those cache that have high volume
        this.dataStreamers = new CacheDataStreamerObjects(this.ignite);
        log.info("Created data streamers");

        // Get cache instances for the caches that have low volume
        entityCache = ignite.getOrCreateCache(IgniteCacheName.ENTITY);
        countryCache = ignite.getOrCreateCache(IgniteCacheName.COUNTRY);
        currencyCache = ignite.getOrCreateCache(IgniteCacheName.CURRENCY);

        // Destroy the temporary cache
        ignite.destroyCache("tempCache");
    }

    @Override
    void loadCounterparty(List<CounterpartyCache> counterparties) {
//        vt.submit(() -> {
        counterparties.forEach(cpty -> dataStreamers.counterpartyStreamer().addData(new CounterpartyCache.Key(cpty.counterpartyCode()), cpty));
        log.info("Loaded counterparties[{}] to cache", counterparties.size());
//        });
    }

    @Override
    void loadCPSlaMapping(List<CounterpartySlaMappingCache> cpsms) {
        cpsms.forEach(cpsm -> dataStreamers.cpSlaMappingStreamer().addData(new CounterpartySlaMappingCache.Key(cpsm.mappingID(), cpsm.getCounterpartyCode()), cpsm));
        log.info("Loaded cpSlaMappings[{}] to cache", cpsms.size());
    }

    @Override
    void loadCPNettingProfile(List<CounterpartyNettingProfileCache> cpnps) {
        cpnps.forEach(cpnp -> dataStreamers.cpNettingProfileStreamer().addData(new CounterpartyNettingProfileCache.Key(cpnp.nettingProfileID(), cpnp.getCounterpartyCode()), cpnp));
        log.info("Loaded cpNettingProfiles[{}] to cache", cpnps.size());
    }

    @Override
    void loadSsi(List<SsiCache> ssis) {
        ssis.forEach(ssi -> dataStreamers.ssiStreamer().addData(new SsiCache.Key(ssi.ssiID(), ssi.getCounterpartyCode()), ssi));
        log.info("Loaded ssis[{}] to cache", ssis.size());
    }

    @Override
    void loadNostro(List<NostroCache> nostros) {
        nostros.forEach(nstr -> dataStreamers.nostroStreamer().addData(new NostroCache.Key(nstr.nostroID(), nstr.getEntityCode()), nstr));
        log.info("Loaded nostros[{}] to cache", nostros.size());
    }

    @Override
    void loadEntity(List<EntityCache> entities) {
        entities.forEach(ent -> entityCache.put(new EntityCache.Key(ent.entityCode()), ent));
        log.info("Loaded entities[{}] to cache", entities.size());
    }

    @Override
    void loadCountry(List<CountryCache> countries) {
        countries.forEach(cntry -> countryCache.put(new CountryCache.Key(cntry.countryCode()), cntry));
        log.info("Loaded countries[{}] to cache", countries.size());
    }

    @Override
    void loadCurrency(List<CurrencyCache> currencies) {
        currencies.forEach(curr -> currencyCache.put(new CurrencyCache.Key(curr.currCode()), curr));
        log.info("Loaded currencies[{}] to cache", currencies.size());
    }
}
