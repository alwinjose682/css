package io.alw.css.tradepublisher.store;

import java.util.List;

public sealed interface TradeStore<T> permits InMemoryTradeStore {
    int[] storeIndexes = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    void add(long retrievalDay, T trdDatum, int storeIdx);

    /// The list of T retrieved are removed from the store and will no longer be available again in the store
    List<T> remove(long retrievalDay, int storeIdx);
}
