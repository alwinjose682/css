package io.alw.css.cashflowconsumer.model;

import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.exception.ExceptionCategory;
import io.alw.css.domain.exception.ExceptionType;
import io.alw.css.serialization.trade.TradeAvro;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDateTime;

@RecordBuilder
public record CashflowRejectionRecord(
        TradeAvro foMsg,
        ExceptionType exceptionType,
        ExceptionCategory exceptionCategory,
        String exceptionSubCategory,
        String msg,
        boolean replayable,
        int numOfRetries,
        LocalDateTime createdDateTime,
        InputBy inputBy
) {
}
