package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.store.CashMessageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/// NOTE: This helper class has variable state
/// TODO: Check if this class can be made common for both cash and confirmation messages. Need to make changes(change CashMessageStore and other cash message specific fields).
final class CashMessageStoreHelper {
    private long lastMessageRetrievalDay;
    private final int maxAmendmentGenerationDelayInDays; // NOTE: Increasing this value will result in retaining the messages requiring amendment for a longer period in the messageStore. Hence, will also result in increased size of the messageStore
    private final int maxAmendmentGenerationDelayInDays_relatedVal;

    private final CashMessageStore msgStore;
    private final RandomGenerator rndm;
    private final CashMessageTemplateHelper msgTemplateHelper;
    private final CashMessageTemplateProperties cashMsgTemplateProps;

    CashMessageStoreHelper(long lastMessageRetrievalDay, int maxAmendmentGenerationDelayInDays, int maxAmendmentGenerationDelayInDaysRelatedVal, CashMessageStore msgStore, RandomGenerator rndm, CashMessageTemplateHelper msgTemplateHelper, CashMessageTemplateProperties cashMsgTemplateProps) {
        this.lastMessageRetrievalDay = lastMessageRetrievalDay;
        this.maxAmendmentGenerationDelayInDays = maxAmendmentGenerationDelayInDays;
        this.maxAmendmentGenerationDelayInDays_relatedVal = maxAmendmentGenerationDelayInDaysRelatedVal;
        this.msgStore = msgStore;
        this.rndm = rndm;
        this.msgTemplateHelper = msgTemplateHelper;
        this.cashMsgTemplateProps = cashMsgTemplateProps;
    }

    List<FoCashMessage> getMessagesToBeAmended() {
        final long currentDay = msgTemplateHelper.dayForMsgTemplate();
        List<FoCashMessage> msgsToBeAmended = new ArrayList<>();
        for (; lastMessageRetrievalDay <= currentDay; ++lastMessageRetrievalDay) {
            List<FoCashMessage> msgs = msgStore.remove(lastMessageRetrievalDay);
            if (msgs != null) {
                msgsToBeAmended.addAll(msgs);
            }
        }
        return msgsToBeAmended;
    }

    /// Randomly select valid amend candidates and save in [CashMessageStore] with a random retrieval day. Random retrieval day depends on [CashMessageTemplate#maxAmendmentGenerationDelayInDays]
    void rndmlySelectValidAmendCandidatesAndSave(List<FoCashMessage> msgs, Predicate<FoCashMessage> inclusionCriteria) {
        long[] amendmentDelayDay = new long[1];
        Predicate<FoCashMessage> finalInclusionCriteria = msg -> inclusionCriteria
                .and(m -> m.cashflowVersion() + m.tradeVersion() <= cashMsgTemplateProps.maxPermittedAmendments())
                .test(msg)
                && (amendmentDelayDay[0] = rndm.nextInt(0, maxAmendmentGenerationDelayInDays)) > maxAmendmentGenerationDelayInDays_relatedVal;

        msgs.stream()
                .filter(finalInclusionCriteria)
                .forEach(msg -> msgStore.add(amendmentDelayDay[0], msg));
    }
}
