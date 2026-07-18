package io.alw.css.tradepublisher.store;

import io.alw.css.tradepublisher.generator.DayTicker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has mutable state
public sealed class StoreHelper<T> permits ConfirmationMatchStore {
    private long lastRetrievalDay;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore

    private final Store<T> store;
    private final RandomGenerator rndm;

    public enum Purpose {
        AMEND(Store.storeIndex[0]), ITEM_SPECIFIC_EVENT(Store.storeIndex[1]);

        private final int storeIdx;

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
            Collection<T> msgs = store.remove(lastRetrievalDay, retrievalPurpose.storeIdx);
            if (msgs != null) {
                return msgs;
            }
        }
        return new ArrayList<>();
    }

    /// Store item in [Store] with a random retrieval day that ranges from `lastMessageRetrievalDay` upto `maxAmendmentGenerationDelayInDays` into the future
    public void storeForFutureRndmRetrievalDay(T msgData, Purpose trdRetrievalPurpose) {
        long futureAmendmentDay = rndm.nextLong(lastRetrievalDay, lastRetrievalDay + maxAmendmentGenerationDelayInDays);
        store.add(futureAmendmentDay, msgData, trdRetrievalPurpose.storeIdx);
    }

    public void storeForFutureRetrievalDay(T msgData, Purpose trdRetrievalPurpose, long retrievalDay) {
        store.add(retrievalDay, msgData, trdRetrievalPurpose.storeIdx);
    }
}
