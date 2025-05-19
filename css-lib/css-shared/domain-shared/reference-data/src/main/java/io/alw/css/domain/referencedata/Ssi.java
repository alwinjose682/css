package io.alw.css.domain.referencedata;

import io.alw.css.domain.cashflow.TradeType;
import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDateTime;

@RecordBuilder
public record Ssi(
        String ssiID,
        int ssiVersion,
        String counterpartyCode, // Counterparty SSI is not associated with an entity.
        int counterpartyVersion,
        String currCode,
        TradeType product, // In CSS, I am considering Cashflow#TradeType same as product (or actually product as Cashflow#TradeType?)
        boolean primary, // There is only one primary SSI, all others are secondary
        // Minimal delivery instruction
        String beneType,
        String bankBic,
        String bankAccount,
        String bankLine1,
        String corrBic,
        String corrAccount,
        String corrLine1,
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
