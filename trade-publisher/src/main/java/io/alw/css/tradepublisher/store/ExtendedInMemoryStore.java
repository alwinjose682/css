package io.alw.css.tradepublisher.store;

import io.alw.css.confirmation.ContextualId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ExtendedInMemoryStore<T extends ContextualId> extends InMemoryStore<T> {
    private static final Logger log = LoggerFactory.getLogger(ExtendedInMemoryStore.class);
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
        if (primaryStorage == null) {
            return new ArrayList<>();
        }

        // Remove the references to the primaryStorage present in secondaryStorage
        Map<Long, Collection<T>> secondaryStorage = getSecondaryStore(storeIdx);
        if (secondaryStorage != null) {
            primaryStorage.forEach(i -> secondaryStorage.remove(i.contextualId()));
        } else {
            log.warn("secondaryStorage is null when primaryStorage is NOT null. This should not happen");
        }

        // Return the removed items
        return primaryStorage;
    }

    /// Removes this single item identified by [ContextualId#contextualId()] from the primary storage. Also remove the entry for the item from the secondary store
    /// The entry for the item in the secondary store is with mapping: item#id to the primaryStorage that contains the item
    public boolean removeById(long id, int storeIdx) {
        Collection<T> primaryStorage = getSecondaryStore(storeIdx) // get the secondary store
                .get(id);// get the primaryStorage where the item with 'id' is stored
        return primaryStorage != null && primaryStorage.remove(id); // remove the specific item from the primaryStorage(for this class the primaryStorage is backed by a HashSet)
    }

    @Override
    protected void addToStorage(Collection<T> primaryStorage, T item, int storeIdx) {
        // Add the item to the primary storage
        super.addToStorage(primaryStorage, item, storeIdx);

        // Add the item to the secondary storage
        getSecondaryStore(storeIdx) // get the secondary store
                .put(item.contextualId(), primaryStorage); // save the primaryStorage, with mapping: item#id to the primaryStorage that contains the item
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
