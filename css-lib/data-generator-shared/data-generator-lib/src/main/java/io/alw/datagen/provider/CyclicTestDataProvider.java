package io.alw.datagen.provider;

import io.alw.datagen.DataGeneratable;

import java.util.List;

public class CyclicTestDataProvider extends AbstractCyclicDataProvider<DataGeneratable> {
    public CyclicTestDataProvider(List<DataGeneratable> testDataList) {
        super(testDataList);
    }
}
