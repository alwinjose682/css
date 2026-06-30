package io.alw.css.tradepublisher.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// [InMemoryTradeStore] is not thread safe. It is intended to be used exclusively by a single thread
public final class InMemoryTradeStore<T> implements TradeStore<T> {
    private final Map<Long, List<T>> store;

    public InMemoryTradeStore() {
        this.store = new HashMap<>();
    }

    @Override
    public void add(long retrievalDay, T trdDatum) {
        final List<T> trdData = store.get(retrievalDay);
        if (trdData == null) {
            List<T> newTrdData = new ArrayList<>();
            newTrdData.add(trdDatum);
            store.put(retrievalDay, newTrdData);
        } else {
            trdData.add(trdDatum);
        }
    }

    @Override
    public List<T> remove(long retrievalDay) {
        return store.remove(retrievalDay);
    }
}
