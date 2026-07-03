package io.alw.css.tradepublisher.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// [InMemoryTradeStore] is not thread safe. It is intended to be used exclusively by a single thread
/// NOTE: Generic array creation is not allowed. Therefor decided to use 2 stores instead of letting the classes that use [TradeStore] to select the number of stores required
public final class InMemoryTradeStore<T> implements TradeStore<T> {
    private final Map<Long, List<T>> store1;
    private final Map<Long, List<T>> store2;

    public InMemoryTradeStore() {
        this.store1 = new HashMap<>();
        this.store2 = new HashMap<>();
    }

    @Override
    public void add(long retrievalDay, T trdDatum, int storeIdx) {
        Map<Long, List<T>> store = getStore(storeIdx);
        final List<T> trdData = store.get(retrievalDay);
        if (trdData == null) {
            List<T> newTrdData = new ArrayList<>();
            newTrdData.add(trdDatum);
            store.put(retrievalDay, newTrdData);
        } else {
            trdData.add(trdDatum);
        }
    }

    private Map<Long, List<T>> getStore(int storeIdx) {
        if (storeIdx == TradeStore.storeIndexes[0]) {
            return store1;
        } else {
            if (storeIdx == TradeStore.storeIndexes[1]) {
                return store2;
            } else {
                throw new RuntimeException("Invalid Trade Store Idx");
            }
        }
    }

    @Override
    public List<T> remove(long retrievalDay, int storeIdx) {
        return getStore(storeIdx).remove(retrievalDay);
    }
}
