package io.alw.datagen.provider;

import java.util.List;
import java.util.function.Supplier;

public interface CyclicDataProvider<T> extends Supplier<T> {
    List<? extends T> dataList();

    int idx();

    T next();

    T current();

    @Override
    default T get() {
        return next();
    }
}
