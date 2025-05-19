package io.alw.datagen.provider;

import io.alw.datagen.TestDataGeneratable;

import java.util.List;

public class CyclicTestDataProvider extends AbstractCyclicDataProvider<TestDataGeneratable> {
    public CyclicTestDataProvider(List<TestDataGeneratable> testDataList) {
        super(testDataList);
    }
}
