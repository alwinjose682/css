package io.alw.css.refdtgtr.provider;

import io.alw.datagen.TestDataGeneratable;
import io.alw.css.refdtgtr.model.TestDataType;
import io.alw.datagen.provider.CyclicTestDataProvider;

import java.util.HashMap;
import java.util.Map;

public final class RefDataProvider {
    private final Map<TestDataType, CyclicTestDataProvider> testDataMap;

    public RefDataProvider() {
        this.testDataMap = new HashMap<>();
        populateData();
    }

    private void populateData() {
        RefDataCollection tdc = RefDataCollection.singleton();
        testDataMap.put(TestDataType.ENTITY, new CyclicTestDataProvider(tdc.entities));
        testDataMap.put(TestDataType.COUNTRY, new CyclicTestDataProvider(tdc.countries));
        testDataMap.put(TestDataType.CURRENCY, new CyclicTestDataProvider(tdc.currencies));
        testDataMap.put(TestDataType.DATA_SET__COUNTRY_STATE_CURRENCY, new CyclicTestDataProvider(tdc.countryStateCurrencyList));
    }

    public TestDataGeneratable next(TestDataType testDataType) {
        return getTestData(testDataType).next();
    }

    public TestDataGeneratable current(TestDataType testDataType) {
        return getTestData(testDataType).current();
    }

    private CyclicTestDataProvider getTestData(TestDataType testDataType) {
        return switch (testDataType) {
            case COUNTRY, CURRENCY, ENTITY, DATA_SET__COUNTRY_STATE_CURRENCY -> testDataMap.get(testDataType);
            case PRODUCT, COUNTERPARTY_TYPE, CSS_NETTING_TYPE, STATE_OR_CITY_OR_STREET_GENERIC_NAMES ->
                    throw new RuntimeException("Unfortunately, you have to use the dataProvider specific to the type of: " + testDataType);
        };
    }
}
