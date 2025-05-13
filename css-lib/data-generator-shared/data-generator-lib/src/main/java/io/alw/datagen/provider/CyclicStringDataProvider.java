package io.alw.datagen.provider;

import java.util.List;

public class CyclicStringDataProvider extends AbstractCyclicDataProvider<String> {
    public CyclicStringDataProvider(List<String> dataList) {
        super(dataList);
    }
}
