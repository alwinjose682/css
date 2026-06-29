package io.alw.css.fosimulator.store;

import java.util.List;

public sealed interface TradeStore<T> permits InMemoryTradeStore {
    void add(long retrievalDay, T trdDatum);

    /// The list of T retrieved are removed from the store and will no longer be available again in the store
    List<T> remove(long retrievalDay);
}
