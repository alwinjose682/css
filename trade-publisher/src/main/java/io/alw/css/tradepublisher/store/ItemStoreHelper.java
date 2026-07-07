package io.alw.css.tradepublisher.store;

import io.alw.css.tradepublisher.generator.DayTicker;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has mutable state
public final class ItemStoreHelper<T> {
    private long lastRetrievalDay;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore

    private final ItemStore<T> msgStore;
    private final RandomGenerator rndm;

    public enum Purpose {
        AMEND(ItemStore.storeIndexes[0]), ITEM_SPECIFIC_EVENT(ItemStore.storeIndexes[1]);

        private final int storeIdx;

        Purpose(int storeIdx) {
            this.storeIdx = storeIdx;
        }
    }

    public ItemStoreHelper(DayTicker lastRetrievalDay, ItemStore<T> msgStore, RandomGenerator rndm) {
        this.lastRetrievalDay = lastRetrievalDay.firstDay();
        this.msgStore = msgStore;
        this.rndm = rndm;
    }

    /// Removes the message data from the store and returns it
    public List<T> retrieve(Purpose trdRetrievalPurpose, long retrievalDay) {
        List<T> msgsToBeAmended = new ArrayList<>();
        for (; lastRetrievalDay <= retrievalDay; ++lastRetrievalDay) {
            List<T> msgs = msgStore.remove(lastRetrievalDay, trdRetrievalPurpose.storeIdx);
            if (msgs != null) {
                msgsToBeAmended.addAll(msgs);
            }
        }
        return msgsToBeAmended;
    }

    /// Store item in [ItemStore] with a random retrieval day that ranges from `lastMessageRetrievalDay` upto `maxAmendmentGenerationDelayInDays` into the future
    public void storeForFutureRndmRetrievalDay(T msgData, Purpose trdRetrievalPurpose) {
        long futureAmendmentDay = rndm.nextLong(lastRetrievalDay, lastRetrievalDay + maxAmendmentGenerationDelayInDays);
        msgStore.add(futureAmendmentDay, msgData, trdRetrievalPurpose.storeIdx);
    }

    public void storeForFutureRetrievalDay(T msgData, Purpose trdRetrievalPurpose, long retrievalDay) {
        msgStore.add(retrievalDay, msgData, trdRetrievalPurpose.storeIdx);
    }
}
