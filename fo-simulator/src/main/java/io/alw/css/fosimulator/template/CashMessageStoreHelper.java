package io.alw.css.fosimulator.template;

import io.alw.css.fosimulator.cashflowgnrtr.DayTicker;
import io.alw.css.fosimulator.store.CashMessageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has mutable state
/// TODO: Check if this class can be made common for both cash and confirmation messages. Need to make changes(change CashMessageStore and other cash message specific fields).
final class CashMessageStoreHelper<T> {
    private long lastMessageRetrievalDay;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore

    private final CashMessageStore<T> msgStore;
    private final RandomGenerator rndm;
    private final CashMessageTemplateHelper msgTemplateHelper;

    CashMessageStoreHelper(DayTicker lastMessageRetrievalDay, CashMessageStore<T> msgStore, RandomGenerator rndm, CashMessageTemplateHelper msgTemplateHelper) {
        this.lastMessageRetrievalDay = lastMessageRetrievalDay.firstDay();
        this.msgStore = msgStore;
        this.rndm = rndm;
        this.msgTemplateHelper = msgTemplateHelper;
    }

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

    /// Store message data in [CashMessageStore] with a random retrieval day that ranges from `lastMessageRetrievalDay` upto `maxAmendmentGenerationDelayInDays` into the future
    void storeMessagesForFutureRndmRetrievalDay(List<T> msgs) {
        long futureAmendmentDay = rndm.nextLong(lastMessageRetrievalDay, lastMessageRetrievalDay + maxAmendmentGenerationDelayInDays);
        msgs.forEach(msg -> msgStore.add(futureAmendmentDay, msg));
    }
}
