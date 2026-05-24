package io.alw.css.fosimulator.template;

import io.alw.css.domain.cashflow.TransactionType;
import io.alw.css.fosimulator.model.properties.CashMessageTemplateProperties;
import io.alw.css.fosimulator.service.RefDataService;
import io.alw.datagen.template.CountAware;

import java.time.LocalDate;
import java.util.random.RandomGenerator;


/// NOTE: This helper class has variable state
final class CashMessageTemplateHelper implements CountAware {
    // Variable values for each template build. Also, these remain un-modified for each template build.
    private long dayForMsgTemplate;

    // Fixed values for each instance of CashMessageTemplate and therefore for MessageTemplateHelper
    private final LocalDate initialValueDate;
    private final TransactionType transactionType;
    private final RandomGenerator rndm;

    // Spring Beans
    final CashMessageTemplateProperties cashMsgTemplateProps;
    private final RefDataService refDataService;

    private long counter;

    CashMessageTemplateHelper(LocalDate initialValueDate, TransactionType transactionType, RandomGenerator rndm, CashMessageTemplateProperties cashMsgTemplateProps, RefDataService refDataService) {
        this.initialValueDate = initialValueDate;
        this.transactionType = transactionType;
        this.rndm = rndm;
        this.cashMsgTemplateProps = cashMsgTemplateProps;
        this.refDataService = refDataService;
        this.counter = 0L;
    }

    /// Check the documentation for [CashMessageTemplate#getRndmValueDate()]
    ///
    /// `numOfTemplateCreationsForValueDateToRemainSameAsCurrentDayCounter` - determines the first N number of templates for which a random number should not be added to the current [CashMessageTemplate#dayForMsgTemplate]
    LocalDate getRndmValueDate(long numOfTemplateCreationsForValueDateToRemainSameAsCurrentDayCounter) {
        if (counter() <= numOfTemplateCreationsForValueDateToRemainSameAsCurrentDayCounter) {
            long daysToAdd = dayForMsgTemplate;
            return initialValueDate.plusDays(daysToAdd);
        } else {
            return getRndmValueDate();
        }
    }

    /// Returns the value date which can randomly range from [CashMessageTemplateProperties#vdBackwardDays] to [CashMessageTemplateProperties#vdForwardDays] with respect to the current [CashMessageTemplate#dayForMsgTemplate].
    /// This means this method can return back valued date as well, but the percentage of back valued cashMessages is configured to be very less.
    LocalDate getRndmValueDate() {
        final long daysToAdd;
        if (isAnNthItem(cashMsgTemplateProps.numOfCfsForABackVdCf())) { // A back valued cashflow will be created only when this becomes true
            daysToAdd = rndm.nextInt(Math.negateExact(cashMsgTemplateProps.vdBackwardDays()), -1);
        } else {
            daysToAdd = dayForMsgTemplate + rndm.nextInt(0, cashMsgTemplateProps.vdForwardDays());
        }
        return initialValueDate.plusDays(daysToAdd);
    }

    LocalDate getRndmFutureValuedDate() {
        return getRndmFutureValueDateRelativeTo(initialValueDate, false, 0);
    }

    /// NOTE: This method can create valueDate higher than cashMsgTemplateProps::vdForwardDays()
    /// NOTE: The method can return a current or future valued date even though the param`isBackValuedDateExpectedAsResult` is set to true
    LocalDate getRndmFutureValueDateRelativeTo(LocalDate givenDate, boolean isBackValuedDateExpectedAsResult, long minimumNumOfDaysIntoFutureRelativeToTheGivenDate) {
        final long daysToAdd;
        if (!isBackValuedDateExpectedAsResult) {
            daysToAdd = dayForMsgTemplate + minimumNumOfDaysIntoFutureRelativeToTheGivenDate + rndm.nextInt(0, 365 + cashMsgTemplateProps.vdForwardDays());
        } else {
            daysToAdd = minimumNumOfDaysIntoFutureRelativeToTheGivenDate;
        }

        return givenDate.plusDays(daysToAdd);
    }

    /// If the resultant value date after adding `daysToAdd` is after `dateRangeEnd`, then `dateRangeEnd` is returned as the result, because the value date returned must be within the given date range
    public LocalDate getFutureValueDate(long daysToAdd, LocalDate dateRangeStart, LocalDate dateRangeEnd) {
        LocalDate resultVD = dateRangeStart.plusDays(dayForMsgTemplate + daysToAdd);
        if (dateRangeStart.isAfter(dateRangeEnd)) {
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

    long dayForMsgTemplate() {
        return dayForMsgTemplate;
    }

    void setDayForMsgTemplate(long day) {
        dayForMsgTemplate = day;
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
