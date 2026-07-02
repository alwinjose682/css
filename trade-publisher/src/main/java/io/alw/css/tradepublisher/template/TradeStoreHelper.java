package io.alw.css.tradepublisher.template;

import io.alw.css.tradepublisher.store.TradeStore;
import io.alw.css.tradepublisher.tradegenerator.DayTicker;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has mutable state
final class TradeStoreHelper<T> {
    private long lastTradeRetrievalDay;
    private static final int maxAmendmentGenerationDelayInDays = 20; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore

    private final TradeStore<T> msgStore;
    private final RandomGenerator rndm;
    private final TradeTemplateHelper msgTemplateHelper;

    enum TradeRetrievalPurpose {
        AMEND(TradeStore.storeIndexes[0]), TRD_SPECIFIC_EVENT(TradeStore.storeIndexes[1]);

        private final int storeIdx;

        TradeRetrievalPurpose(int storeIdx) {
            this.storeIdx = storeIdx;
        }
    }

    TradeStoreHelper(DayTicker lastTradeRetrievalDay, TradeStore<T> msgStore, RandomGenerator rndm, TradeTemplateHelper msgTemplateHelper) {
        this.lastTradeRetrievalDay = lastTradeRetrievalDay.firstDay();
        this.msgStore = msgStore;
        this.rndm = rndm;
        this.msgTemplateHelper = msgTemplateHelper;
    }

    /// Removes the message data from the store and returns it
    List<T> retrieveTradesForCurrentDay(TradeRetrievalPurpose trdRetrievalPurpose) {
        final long currentDay = msgTemplateHelper.currentDayForTrdTemplate();
        List<T> msgsToBeAmended = new ArrayList<>();
        for (; lastTradeRetrievalDay <= currentDay; ++lastTradeRetrievalDay) {
            List<T> msgs = msgStore.remove(lastTradeRetrievalDay, trdRetrievalPurpose.storeIdx);
            if (msgs != null) {
                msgsToBeAmended.addAll(msgs);
            }
        }
        return msgsToBeAmended;
    }

    /// Store trade in [TradeStore] with a random retrieval day that ranges from `lastMessageRetrievalDay` upto `maxAmendmentGenerationDelayInDays` into the future
    void storeTradeForFutureRndmRetrievalDay(T msgData, TradeRetrievalPurpose trdRetrievalPurpose) {
        long futureAmendmentDay = rndm.nextLong(lastTradeRetrievalDay, lastTradeRetrievalDay + maxAmendmentGenerationDelayInDays);
        msgStore.add(futureAmendmentDay, msgData, trdRetrievalPurpose.storeIdx);
    }

    void storeTradeForFutureRetrievalDay(T msgData, TradeRetrievalPurpose trdRetrievalPurpose, long retrievalDay) {
        msgStore.add(retrievalDay, msgData, trdRetrievalPurpose.storeIdx);
    }
}
