package io.alw.css.tradeconsumer.cashflow.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    public static LocalDate formatValueDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
    }
}
