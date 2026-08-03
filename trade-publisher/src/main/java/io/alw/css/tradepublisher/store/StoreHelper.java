package io.alw.css.tradepublisher.store;

import io.alw.css.tradepublisher.generator.DayTicker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has mutable state
public sealed class StoreHelper<T> permits ExtendedStoreHelper {
    private long lastRetrievalDay;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore

    protected final Store<T> store;
    private final RandomGenerator rndm;

    public enum Purpose {
        AMEND(Store.storeIndex[0]), ITEM_SPECIFIC_EVENT(Store.storeIndex[1]);

        protected final int storeIdx; // protected visibility for enum's field works, this is now accessible by StoreHelper's child class!!!

        Purpose(int storeIdx) {
            this.storeIdx = storeIdx;
        }
    }

    public StoreHelper(DayTicker dayTicker, Store<T> store, RandomGenerator rndm) {
        this.lastRetrievalDay = dayTicker.firstDay();
        this.store = store;
        this.rndm = rndm;
    }

    /// Removes the message data from the store and returns it
    public Collection<T> remove(Purpose retrievalPurpose, long retrievalDay) {
        for (; lastRetrievalDay <= retrievalDay; ++lastRetrievalDay) {
            Collection<T> item = store.remove(lastRetrievalDay, retrievalPurpose.storeIdx);
            if (item != null) {
                return item;
            }
        }
        return new ArrayList<>();
    }

    /// Store the item in [Store] with a random retrieval day that ranges from `lastMessageRetrievalDay` upto `maxAmendmentGenerationDelayInDays` into the future
    public void storeForFutureRndmRetrievalDay(T item, Purpose purpose) {
        long futureAmendmentDay = rndm.nextLong(lastRetrievalDay, lastRetrievalDay + maxAmendmentGenerationDelayInDays);
        store.add(futureAmendmentDay, item, purpose.storeIdx);
    }

    public void storeForFutureRetrievalDay(T item, Purpose purpose, long retrievalDay) {
        store.add(retrievalDay, item, purpose.storeIdx);
    }
}
