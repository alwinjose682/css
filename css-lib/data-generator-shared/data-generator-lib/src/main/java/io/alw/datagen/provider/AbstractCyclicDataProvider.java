package io.alw.datagen.provider;

import java.util.List;

public class AbstractCyclicDataProvider<T> implements CyclicDataProvider<T> {
    private int idx;
    private final List<T> dataList;

    protected AbstractCyclicDataProvider(List<T> dataList) {
        this.idx = 0;
        this.dataList = dataList;
    }

    @Override
    public List<T> dataList() {
        return dataList;
    }

    @Override
    public int idx() {
        return idx;
    }

    @Override
    public T next() {
        if (idx == dataList.size()) {
            idx = 0;
        }
        return dataList.get(idx++);
    }

    @Override
    public T current() {
        return dataList.get(idx);
    }
}
