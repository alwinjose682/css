package io.alw.css.refdataloader.service;

import io.alw.css.domain.referencedata.Country;
import io.alw.css.domain.referencedata.Currency;
import io.alw.css.model.referencedata.*;
import io.alw.css.infra.ignitecache.CacheTableDefinition;
import io.alw.css.refdataloader.mapper.cache.*;
import io.alw.css.refdataloader.mapper.jpa.*;
import io.alw.css.refdataloader.model.jpa.*;
import io.alw.css.refdtgtr.model.CounterpartyAndDependentData;
import io.alw.css.refdtgtr.model.EntityAndDependentData;
import io.alw.css.refdtgtr.model.GeneratedReferenceData;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public sealed abstract class CacheDataLoader permits SimpleCacheDataLoader, StreamingCacheDataLoader {
    private static final Logger log = LoggerFactory.getLogger(CacheDataLoader.class);

    abstract protected void initIgniteClient();

    abstract public void releaseResources();

    abstract void createCaches();

    abstract void loadCurrency(List<CurrencyCache> currencies);

    abstract void loadCounterparty(List<CounterpartyCache> counterparties);

    abstract void loadCPSlaMapping(List<CounterpartySlaMappingCache> cpsms);

    abstract void loadCPNettingProfile(List<CounterpartyNettingProfileCache> cpnps);

    abstract void loadSsi(List<SsiCache> ssis);

    abstract void loadEntity(List<EntityCache> entities);

    abstract void loadNostro(List<NostroCache> nostros);

    abstract void loadCountry(List<CountryCache> countries);

    protected List<SqlFieldsQuery> getCacheTableDdlQueryObjs() {
        CacheTableDefinition cacheTableDefinition = CacheTableDefinition.get();
        return List.of(new SqlFieldsQuery(cacheTableDefinition.ENTITY),
                new SqlFieldsQuery(cacheTableDefinition.NOSTRO),
                new SqlFieldsQuery(cacheTableDefinition.COUNTERPARTY),
                new SqlFieldsQuery(cacheTableDefinition.CP_NETTING_PROFILE),
                new SqlFieldsQuery(cacheTableDefinition.CP_SLA_MAPPING),
                new SqlFieldsQuery(cacheTableDefinition.SSI),
                new SqlFieldsQuery(cacheTableDefinition.COUNTRY),
                new SqlFieldsQuery(cacheTableDefinition.CURRENCY)
                // TODO: Create indexes
        );
    }

    /// NOTE: The dependent data like nostro, ssi, nettingProfile and slaMapping are loaded for each Entity and Counterparty
    /// They are NOT loaded as a whole, ie; for example, ssi of all counterparties are not collected together in a list and loaded into cache
    /// There is no overhead with current approach as data is loaded to cache using ignite's put api and not via data streaming
    void load(GeneratedReferenceData generatedReferenceData) {
        List<Country> countries = generatedReferenceData.countries();
        List<Currency> currencies = generatedReferenceData.currencies();
        List<EntityAndDependentData> entityAndDependentData = generatedReferenceData.entityAndDependentData();
        List<CounterpartyAndDependentData> counterpartyAndDependentData = generatedReferenceData.counterpartyAndDependentData();
        List<EntityCache> entityCacheList = new ArrayList<>();
        List<CounterpartyCache> counterpartyCacheList = new ArrayList<>();

        loadCountry(countries.stream().map(CountryCacheMapper.instance()::domainToCache).toList());
        loadCurrency(currencies.stream().map(CurrencyCacheMapper.instance()::domainToCache).toList());
        entityAndDependentData.forEach(entAndDep -> {
            EntityCache entityCache = EntityCacheMapper.instance().domainToCache(entAndDep.entity());
            entityCacheList.add(entityCache);
            List<NostroCache> nostros = entAndDep.nostros().stream().map(NostroCacheMapper.instance()::domainToCache).toList();
            loadNostro(nostros);
        });
        loadEntity(entityCacheList);
        counterpartyAndDependentData.forEach(cptyAndDep -> {
            CounterpartyCache cpty = CounterpartyCacheMapper.mapper().domainToCache(cptyAndDep.counterparty());
            counterpartyCacheList.add(cpty);
            loadCPNettingProfile(cptyAndDep.counterpartyNettingProfiles().stream().map(CounterpartyNettingProfileCacheMapper.instance()::domainToCache).toList());
            loadCPSlaMapping(cptyAndDep.counterpartySlaMappings().stream().map(CounterpartySlaMappingCacheMapper.instance()::domainToCache).toList());
            loadSsi(cptyAndDep.ssis().stream().map(SsiCacheMapper.instance()::domainToCache).toList());
        });
        loadCounterparty(counterpartyCacheList);
    }

    <T extends ReferenceDataJpaEntity> void loadDataFromDB(Supplier<List<T>> dbDataSupplier) {
        int i = 1;
        List<T> page = dbDataSupplier.get();
        log.trace("Fetched data page: 1 with {} items", page.size());
        while (!page.isEmpty()) {
            List<T> page1 = page;
            // TODO: load page in a new thread. Do this for each individual method that supports data streaming
            loadDataPageIntoCache(page1);
            page = dbDataSupplier.get();
            ++i;
            log.trace("Fetched data page: {} with {} items", i, page.size());
        }
        log.debug("Total pages read: {}", i - 1);
    }

    private <T extends ReferenceDataJpaEntity> void loadDataPageIntoCache(List<T> rdp) {
        T refData = rdp.get(0);
        switch (refData) {
            case EntityEntity _ -> loadEntity(rdp.stream().map(rd -> EntityEntityMapper.instance().entityToCache((EntityEntity) rd)).toList());
            case NostroEntity _ -> loadNostro(rdp.stream().map(rd -> NostroEntityMapper.instance().entityToCache((NostroEntity) rd)).toList());
            case CounterpartyEntity _ -> loadCounterparty(rdp.stream().map(rd -> CounterpartyEntityMapper.instance().entityToCache((CounterpartyEntity) rd)).toList());
            case SsiEntity _ -> loadSsi(rdp.stream().map(rd -> SsiEntityMapper.instance().entityToCache((SsiEntity) rd)).toList());
            case CounterpartyNettingProfileEntity _ ->
                    loadCPNettingProfile(rdp.stream().map(rd -> CounterpartyNettingProfileEntityMapper.instance().entityToCache((CounterpartyNettingProfileEntity) rd)).toList());
            case CounterpartySlaMappingEntity _ -> loadCPSlaMapping(rdp.stream().map(rd -> CounterpartySlaMappingEntityMapper.instance().entityToCache((CounterpartySlaMappingEntity) rd)).toList());
            default -> throw new IllegalStateException("Unexpected type : " + refData);
        }
    }
}
