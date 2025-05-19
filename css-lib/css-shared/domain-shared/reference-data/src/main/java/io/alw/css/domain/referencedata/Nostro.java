package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/// NOTE: The cutOffTime value of other currencies is determined relative to the Deutschland UTC time, ie UTC+1/CET
@RecordBuilder
public record Nostro(
        String nostroID,
        int nostroVersion,
        String entityCode,
        int entityVersion,
        String currCode,
        String secondaryLedgerAccount, // sla is NOT unique for a nostroID. sla can be changed by amending the nostro
        boolean primary, // There is only one primary nostro, all others are secondary
        // Minimal delivery instruction
        String beneBic,
        String bankBic,
        String bankAccount,
        String bankLine1,
        String corrBic,
        String corrAccount,
        String corrLine1,
        // Payment release window
        LocalTime cutOffTime, // The end time of payment release window
        int cutInHoursOffset, // The number of hours prior to cutOffTime
        BigDecimal paymentLimit,
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
