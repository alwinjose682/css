package io.alw.css.tradepublisher.store;

import io.alw.css.confirmation.LongId;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class ExtendedInMemoryStore<T extends LongId> extends InMemoryStore<T> {
    private final Map<Long, Collection<T>> store1;
    private final Map<Long, Collection<T>> store2;

    public ExtendedInMemoryStore() {
        super();
        this.store1 = new HashMap<>();
        this.store2 = new HashMap<>();
    }

    /// Overridden to remove the references to the primaryStorage present in secondaryStorage
    @Override
    public Collection<T> remove(long retrievalDay, int storeIdx) {
        Collection<T> primaryStorage = super.remove(retrievalDay, storeIdx);
        Map<Long, Collection<T>> secondaryStorage = getSecondaryStore(storeIdx);

        // Remove the references to the primaryStorage present in secondaryStorage
        primaryStorage.forEach(i -> secondaryStorage.remove(i.id()));

        // Return the removed items
        return primaryStorage;
    }

    /// Removes this single item identified by [LongId#id()] from the primary storage. Also remove the entry for the item from the secondary store
    /// The entry for the item in the secondary store is with mapping: item#id to the primaryStorage that contains the item
    public void removeById(long id, int storeIdx) {
        getSecondaryStore(storeIdx) // get the secondary store
                .get(id) // get the primaryStorage where the item with 'id' is stored
                .remove(id); // remove the specific item from the primaryStorage(for this class the primaryStorage is backed by a HashSet)
    }

    @Override
    protected void addToStorage(Collection<T> primaryStorage, T item, int storeIdx) {
        // Add the item to the primary storage
        super.addToStorage(primaryStorage, item, storeIdx);

        // Add the item to the secondary storage
        getSecondaryStore(storeIdx) // get the secondary store
                .put(item.id(), primaryStorage); // save the primaryStorage, with mapping: item#id to the primaryStorage that contains the item
    }

    @Override
    protected Collection<T> createStorage(int storeIdx) {
        return new HashSet<>();
    }

    private final Map<Long, Collection<T>> getSecondaryStore(int storeIdx) {
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
}
