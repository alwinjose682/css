package io.alw.css.fosimulator.template;

import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TransactionType;
import io.alw.css.fosimulator.model.TradeEventActionPair;
import io.alw.css.fosimulator.model.properties.TradeTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.css.fosimulator.template.domain.MmTradeEvent;
import io.alw.css.fosimulator.template.domain.TradeEventActionRecord;
import io.alw.css.fosimulator.template.domain.TradeEventTypeRecord;
import io.alw.datagen.template.CountAware;

import java.time.LocalDate;
import java.util.random.RandomGenerator;

import static io.alw.css.domain.common.TradeEventAction.*;
import static io.alw.css.domain.common.TradeEventType.*;


/// NOTE: This helper class has mutable state
public final class TradeTemplateHelper implements CountAware {
    // Variable values for each template build. Also, these remain un-modified for each template build.
    private long dayForMsgTemplate;

    // Fixed values for each instance of TradeTemplate and therefore for MessageTemplateHelper
    private final LocalDate initialValueDate;
    private final TransactionType transactionType;
    private final RandomGenerator rndm;

    // Spring Beans
    final TradeTemplateProperties trdTemplateProps;
    private final RefDataService refDataService;

    private long counter;

    TradeTemplateHelper(LocalDate initialValueDate, TransactionType transactionType, RandomGenerator rndm, TradeTemplateProperties trdTemplateProps, RefDataService refDataService) {
        this.initialValueDate = initialValueDate;
        this.transactionType = transactionType;
        this.rndm = rndm;
        this.trdTemplateProps = trdTemplateProps;
        this.refDataService = refDataService;
        this.counter = 0L;
    }

    /// Check the documentation for [TradeTemplate#getRndmValueDate()]
    ///
    /// `numOfTemplateCreationsForValueDateToRemainSameAsCurrentDayCounter` - determines the first N number of templates for which a random number should not be added to the current [TradeTemplate#dayForMsgTemplate]
    LocalDate getRndmValueDate(long numOfTemplateCreationsForValueDateToRemainSameAsCurrentDayCounter) {
        if (counter() <= numOfTemplateCreationsForValueDateToRemainSameAsCurrentDayCounter) {
            long daysToAdd = dayForMsgTemplate;
            return initialValueDate.plusDays(daysToAdd);
        } else {
            return getRndmValueDate();
        }
    }

    /// Returns the value date which can randomly range from [TradeTemplateProperties#vdBackwardDays] to [TradeTemplateProperties#vdForwardDays] with respect to the current [TradeTemplate#dayForMsgTemplate].
    /// This means this method can return back valued date as well, but the percentage of back valued trades is configured to be very less.
    LocalDate getRndmValueDate() {
        final long daysToAdd;
        if (isAnNthItem(trdTemplateProps.numOfCfsForABackVdCf())) { // A back valued trade will be created only when this becomes true
            daysToAdd = rndm.nextInt(Math.negateExact(trdTemplateProps.vdBackwardDays()), -1);
        } else {
            daysToAdd = dayForMsgTemplate + rndm.nextInt(0, trdTemplateProps.vdForwardDays());
        }
        return initialValueDate.plusDays(daysToAdd);
    }

    LocalDate getRndmFutureValuedDate() {
        return getRndmFutureValueDateRelativeTo(initialValueDate, false, 0);
    }

    /// NOTE: This method can create valueDate higher than TradeTemplateProps::vdForwardDays()
    /// NOTE: The method can return a current or future valued date even though the param`isBackValuedDateExpectedAsResult` is set to true
    LocalDate getRndmFutureValueDateRelativeTo(LocalDate givenDate, boolean isBackValuedDateExpectedAsResult, long minimumNumOfDaysIntoFutureRelativeToTheGivenDate) {
        final long daysToAdd;
        if (!isBackValuedDateExpectedAsResult) {
            daysToAdd = dayForMsgTemplate + minimumNumOfDaysIntoFutureRelativeToTheGivenDate + rndm.nextInt(0, 365 + trdTemplateProps.vdForwardDays());
        } else {
            daysToAdd = minimumNumOfDaysIntoFutureRelativeToTheGivenDate;
        }

        return givenDate.plusDays(daysToAdd);
    }

    /// If the resultant value date after adding `daysToAdd` is after `dateRangeEnd`, then `dateRangeEnd` is returned as the result, because the value date returned must be within the given date range
    public LocalDate getFutureValueDate(long daysToAdd, LocalDate dateRangeStart, LocalDate dateRangeEnd) {
        LocalDate resultVD = dateRangeStart.plusDays(daysToAdd);
        if (resultVD.isAfter(dateRangeEnd)) {
            return dateRangeEnd;
        } else {
            return resultVD;
        }
    }

    String getCounterpartyCorrespondingToTransactionType() {
        return isInternalTransaction()
                ? refDataService.internalCounterparty(rndm)
                : refDataService.externalCounterparty(rndm);
    }

    String getCounterpartyCorrespondingToTransactionTypeOtherThan(String counterpartyCodeToAvoid) {
        return isInternalTransaction()
                ? refDataService.internalCounterpartyOtherThan(rndm, counterpartyCodeToAvoid)
                : refDataService.externalCounterpartyOtherThan(rndm, counterpartyCodeToAvoid);
    }

    boolean isInterbookTransaction() {
        return transactionType == TransactionType.INTER_BOOK;
    }

    boolean isInternalTransaction() {
        return transactionType == TransactionType.INTER_BOOK || transactionType == TransactionType.INTER_BRANCH || transactionType == TransactionType.INTER_COMPANY;
    }

    long currentDayForMsgTemplate() {
        return dayForMsgTemplate;
    }

    LocalDate currentDateForMsgTemplate() {
        return initialValueDate.plusDays(dayForMsgTemplate);
    }

    void setDayForMsgTemplate(long day) {
        dayForMsgTemplate = day;
    }

    TradeEventActionPair determineNextTradeEventAndActionForCommonEvents(RandomGenerator rbdm, TradeEventType standardEvent, TradeEventAction standardAction) {
        int num = rndm.nextInt(1, 100);
        TradeEventTypeRecord event = TradeEventTypeRecord.getCorrespondingTradeEventRecord(standardEvent);
        TradeEventActionRecord action = TradeEventActionRecord.getCorrespondingTradeEventAction(standardAction);

        return switch (event) {
            case TradeEventTypeRecord.NEW_TRADE _ when num > 30 -> new TradeEventActionPair(AMEND, ADD);
            case TradeEventTypeRecord.NEW_TRADE _ when num > 10 -> new TradeEventActionPair(CANCEL, ADD);
            case TradeEventTypeRecord.NEW_TRADE _ -> new TradeEventActionPair(REBOOK, ADD);
            case TradeEventTypeRecord.REBOOK _ when num > 10 -> new TradeEventActionPair(AMEND, ADD);
            case TradeEventTypeRecord.REBOOK _ -> new TradeEventActionPair(CANCEL, ADD);
            case TradeEventTypeRecord.AMEND _ -> switch (action) {
                case TradeEventActionRecord.ADD _ when num > 30 -> new TradeEventActionPair(AMEND, MODIFY);
                case TradeEventActionRecord.ADD _ when num > 20 -> new TradeEventActionPair(CANCEL, ADD);
                case TradeEventActionRecord.ADD _ -> new TradeEventActionPair(REBOOK, ADD);
                case TradeEventActionRecord.MODIFY _ when num > 40 -> new TradeEventActionPair(AMEND, MODIFY);
                case TradeEventActionRecord.MODIFY _ when num > 30 -> new TradeEventActionPair(AMEND, REMOVE);
                case TradeEventActionRecord.MODIFY _ when num > 10 -> new TradeEventActionPair(REBOOK, ADD);
                case TradeEventActionRecord.MODIFY _ -> new TradeEventActionPair(CANCEL, ADD);
                case TradeEventActionRecord.REMOVE _ when num > 30 -> new TradeEventActionPair(AMEND, ADD);
                case TradeEventActionRecord.REMOVE _ -> new TradeEventActionPair(AMEND, ADD);
            };
            case TradeEventTypeRecord.CANCEL _ -> throw new RuntimeException("Attempt to amend a cancelled trade is invalid");
            case MmTradeEvent _ -> throw new RuntimeException("Invalid trade event");
        };
    }

    @Override
    public long counter() {
        return this.counter;
    }

    @Override
    public void incrementCounter() {
        ++counter;
    }
}
