package io.alw.datagen.provider;

import java.util.List;

public interface CyclicDataProvider<T> {
    List<? extends T> dataList();

    int idx();

    T next();

    T current();
}
