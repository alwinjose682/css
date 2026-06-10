package io.alw.css.fosimulator.store;

import java.util.*;

/// [InMemoryCashMessageStore] is not thread safe. It is intended to be used exclusively by a single thread
public final class InMemoryCashMessageStore<T> implements CashMessageStore<T> {
    private final Map<Long, List<T>> store;

    public InMemoryCashMessageStore() {
        this.store = new HashMap<>();
    }

    @Override
    public void add(long retrievalDay, T cashMsgDatum) {
        final List<T> cashMsgData = store.get(retrievalDay);
        if (cashMsgData == null) {
            List<T> newCashMsgData = new ArrayList<>();
            newCashMsgData.add(cashMsgDatum);
            store.put(retrievalDay, newCashMsgData);
        } else {
            cashMsgData.add(cashMsgDatum);
        }
    }

    @Override
    public List<T> remove(long retrievalDay) {
        return store.remove(retrievalDay);
    }
}
