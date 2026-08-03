package io.alw.css.tradepublisher.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/// [InMemoryStore] is not thread safe. It is intended to be used exclusively by a single thread
/// NOTE: Generic array creation is not possible. Therefor decided to use 2 stores instead of letting the classes that use [Store] to select the number of stores required
public sealed class InMemoryStore<T> implements Store<T> permits ExtendedInMemoryStore {
    private final Map<Long, Collection<T>> store1;
    private final Map<Long, Collection<T>> store2;

    public InMemoryStore() {
        this.store1 = new HashMap<>();
        this.store2 = new HashMap<>();
    }

    @Override
    public final void add(long retrievalDay, T item, int storeIdx) {
        Map<Long, Collection<T>> store = getStorage(storeIdx);
        final Collection<T> storage = store.get(retrievalDay);
        if (storage == null) {
            Collection<T> newStorage = createStorage(storeIdx); // Invokes this's or child's overridden method depending on the runtime object
            addToStorage(newStorage,item, storeIdx);
            store.put(retrievalDay, newStorage);
        } else {
            addToStorage(storage,item, storeIdx);
        }
    }

    protected void addToStorage(Collection<T> storage, T item, int storeIdx) {
        storage.add(item);
    }

    @Override
    public Collection<T> remove(long retrievalDay, int storeIdx) {
        return getStorage(storeIdx).remove(retrievalDay);
    }

    private final Map<Long, Collection<T>> getStorage(int storeIdx) {
        if (storeIdx == Store.storeIndex[0]) {
            return store1;
        } else {
            if (storeIdx == Store.storeIndex[1]) {
                return store2;
            } else {
                throw new RuntimeException("Invalid Store Idx");
            }
        }
    }

    protected Collection<T> createStorage(int storeIdx) {
        return new ArrayList<>();
    }
}
