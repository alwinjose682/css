package io.alw.css.refdataloader.mapper;

import io.alw.css.domain.common.YesNo;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

//https://mapstruct.org/documentation/stable/reference/html/
public final class MapperUtil {

    public static YesNo booleanToYesNo(boolean b) {
        return b ? YesNo.Y : YesNo.N;
    }

    public static boolean yesNoToBoolean(YesNo yesNo) {
        return yesNo == YesNo.Y;
    }

    public static Timestamp localDateTimeToSqlTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static LocalDateTime sqlTimestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    public static Time localTimeToSqlTime(LocalTime localTime) {
        return Time.valueOf(localTime);
    }

    public static LocalTime sqlTimeToLocalTime(Time time) {
        return time.toLocalTime();
    }
}
