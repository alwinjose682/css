package io.alw.css.fosimulator.store;

import java.util.List;

public sealed interface CashMessageStore<T> permits InMemoryCashMessageStore {
    void add(long retrievalDay, T cashMsgDatum);

    /// The list of T retrieved are removed from the store and will no longer be available again in the store
    List<T> remove(long retrievalDay);
}
