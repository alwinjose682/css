package io.alw.css.tradepublisher.template;

import io.alw.css.tradepublisher.store.TradeStore;
import io.alw.css.tradepublisher.tradegenerator.DayTicker;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has mutable state
final class TradeStoreHelper<T> {
    private long lastMessageRetrievalDay;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore

    private final TradeStore<T> msgStore;
    private final RandomGenerator rndm;
    private final TradeTemplateHelper msgTemplateHelper;

    TradeStoreHelper(DayTicker lastMessageRetrievalDay, TradeStore<T> msgStore, RandomGenerator rndm, TradeTemplateHelper msgTemplateHelper) {
        this.lastMessageRetrievalDay = lastMessageRetrievalDay.firstDay();
        this.msgStore = msgStore;
        this.rndm = rndm;
        this.msgTemplateHelper = msgTemplateHelper;
    }

    /// Removes the message data from the store and returns it
    List<T> retrieveMessagesForCurrentDay() {
        final long currentDay = msgTemplateHelper.currentDayForMsgTemplate();
        List<T> msgsToBeAmended = new ArrayList<>();
        for (; lastMessageRetrievalDay <= currentDay; ++lastMessageRetrievalDay) {
            List<T> msgs = msgStore.remove(lastMessageRetrievalDay);
            if (msgs != null) {
                msgsToBeAmended.addAll(msgs);
            }
        }
        return msgsToBeAmended;
    }

    /// Store message data in [TradeStore] with a random retrieval day that ranges from `lastMessageRetrievalDay` upto `maxAmendmentGenerationDelayInDays` into the future
    void storeMessageDataForFutureRndmRetrievalDay(T msgData) {
        long futureAmendmentDay = rndm.nextLong(lastMessageRetrievalDay, lastMessageRetrievalDay + maxAmendmentGenerationDelayInDays);
        msgStore.add(futureAmendmentDay, msgData);
    }
}
