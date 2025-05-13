package io.alw.css.refdtgtr.provider;

import io.alw.css.domain.cashflow.TradeType;
import io.alw.css.domain.common.CssNettingType;
import io.alw.datagen.TestDataGeneratable;
import io.alw.css.domain.referencedata.Country;
import io.alw.css.domain.referencedata.Currency;
import io.alw.css.domain.referencedata.Entity;
import io.alw.css.refdtgtr.domain.CounterpartyType;
import io.alw.css.refdtgtr.domain.PreDefinedTestData;
import io.alw.css.refdtgtr.model.CountryStateCurrency;
import io.alw.css.refdtgtr.model.EntityCurrencyState;
import io.alw.datagen.provider.CyclicStringDataProvider;
import io.alw.datagen.provider.CyclicTestDataProvider;

import java.util.*;

public final class RefDataCollection {
    private static RefDataCollection refDataCollection;
    public final List<TestDataGeneratable> entities;
    public final List<TestDataGeneratable> countries;
    public final List<TestDataGeneratable> currencies;
    public final List<String> counterpartyTypeNames;
    public final List<String> productNames;
    public final List<String> cssNettingTypeNames;
    public final List<String> genericStateOrCityNames;
    public final Map<String, Currency> currencyMap;
    //
    public final List<TestDataGeneratable> countryStateCurrencyList;
    public final List<EntityCurrencyState> entityCurrencyStateList;

    private RefDataCollection() {
        PreDefinedTestData preDefinedTestData = PreDefinedTestData.singleton();
        this.entities = preDefinedTestData.entities.stream().map(e -> (TestDataGeneratable) e).toList();
        this.countries = preDefinedTestData.countries.stream().map(c -> (TestDataGeneratable) c).toList();
        this.currencies = preDefinedTestData.currencies.stream().map(c -> (TestDataGeneratable) c).toList();
        this.genericStateOrCityNames = new ArrayList<>();
        this.counterpartyTypeNames = Arrays.stream(CounterpartyType.values()).map(Enum::name).toList();
        this.productNames = Arrays.stream(TradeType.values()).map(Enum::name).toList();
        this.cssNettingTypeNames = Arrays.stream(CssNettingType.values()).map(Enum::name).toList();
        this.currencyMap = getCurrencyMap();
        // initialise last
        this.countryStateCurrencyList = getCountryStateCurrencyList(this.entities, this.countries, this.currencies);
        this.entityCurrencyStateList = getEntityCurrencyStateList();
    }

    private List<EntityCurrencyState> getEntityCurrencyStateList() {
        List<EntityCurrencyState> entityCurrencyStateList = new ArrayList<>();
        CyclicStringDataProvider cyclicCityOrStateNameProvider = new CyclicStringDataProvider(this.genericStateOrCityNames);

        for (TestDataGeneratable tdg : this.entities) {
            Entity entity = (Entity) tdg;
            var currency = (Currency) currencies.stream()
                    .filter(curr -> ((Currency) curr).currCode().equalsIgnoreCase(entity.currCode()))
                    .findAny().orElseThrow();

            EntityCurrencyState entityCurrencyState = new EntityCurrencyState(entity, currency, cyclicCityOrStateNameProvider.next());
            entityCurrencyStateList.add(entityCurrencyState);
        }
        return entityCurrencyStateList;
    }

    public static RefDataCollection singleton() {
        if (refDataCollection == null) {
            synchronized (RefDataCollection.class) {
                if (refDataCollection == null) {
                    refDataCollection = new RefDataCollection();
                }
                return refDataCollection;
            }
        }
        return refDataCollection;
    }

    private Map<String, Currency> getCurrencyMap() {
        Map<String, Currency> currCodeToCurrencyMap = new HashMap<>();
        this.currencies.forEach(curr -> {
            Currency currency = (Currency) curr;
            currCodeToCurrencyMap.put(currency.currCode(), currency);
        });
        return Collections.unmodifiableMap(currCodeToCurrencyMap);
    }

    private List<TestDataGeneratable> getCountryStateCurrencyList(List<TestDataGeneratable> entities, List<TestDataGeneratable> countries, List<TestDataGeneratable> currencies) {
        List<TestDataGeneratable> countryStateCurrencyList = new ArrayList<>();
        CyclicTestDataProvider entityTestDataProvider = new CyclicTestDataProvider(entities);
        Map<String, Country> countryMap = new HashMap<>();
        countries.forEach(td -> {
            Country country = (Country) td;
            countryMap.put(country.countryCode(), country);
        });
        Map<String, Currency> currencyMap = new HashMap<>();
        currencies.forEach(td -> {
            Currency curr = (Currency) td;
            currencyMap.put(curr.countryCode(), curr);
        });

        String rawData = PreDefinedTestData.regionCountryStateRawData();
        String[] lines = rawData.split("\n");
        for (String state : lines) {
            this.genericStateOrCityNames.add(state); // populate genericStateOrCityName list

            Entity entity = (Entity) entityTestDataProvider.next();
            String countryCode = entity.countryCode();
            Country country = countryMap.get(countryCode);
            Currency currency = currencyMap.get(countryCode);

            countryStateCurrencyList.add(new CountryStateCurrency(country, state, currency));
        }

        return Collections.unmodifiableList(countryStateCurrencyList);
    }
}
