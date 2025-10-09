package io.alw.css.refdtgtr.provider;

import io.alw.css.refdtgtr.model.TestDataType;
import io.alw.datagen.provider.CyclicStringDataProvider;

public class StringRefDataProvider {
    private final CyclicStringDataProvider counterpartyTypeProvider;
    private final CyclicStringDataProvider genericStateCityOrStreetNameProvider;
    private final CyclicStringDataProvider productNameProvider;
    private final CyclicStringDataProvider cssNettingTypeNameProvider;

    public StringRefDataProvider() {
        RefDataCollection tdc = RefDataCollection.singleton();
        this.counterpartyTypeProvider = new CyclicStringDataProvider(tdc.counterpartyTypeNames);
        this.genericStateCityOrStreetNameProvider = new CyclicStringDataProvider(tdc.genericStateOrCityNames);
        this.productNameProvider = new CyclicStringDataProvider(tdc.productNames);
        this.cssNettingTypeNameProvider = new CyclicStringDataProvider(tdc.cssNettingTypeNames);
    }

    public String next(TestDataType testDataType) {
        return getTestData(testDataType).next();
    }

    public String current(TestDataType testDataType) {
        return getTestData(testDataType).current();
    }

    private CyclicStringDataProvider getTestData(TestDataType testDataType) {
        return switch (testDataType) {
            case COUNTERPARTY_TYPE -> counterpartyTypeProvider;
            case STATE_OR_CITY_OR_STREET_GENERIC_NAMES -> genericStateCityOrStreetNameProvider;
            case PRODUCT -> productNameProvider;
            case CSS_NETTING_TYPE -> cssNettingTypeNameProvider;
            case DATA_SET__COUNTRY_STATE_CURRENCY, COUNTRY, CURRENCY, ENTITY -> throw new RuntimeException("Unfortunately, you have to use the dataProvider specific to the type of: " + testDataType);
        };
    }
}
