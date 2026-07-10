package io.alw.css.confirmation;

import io.alw.css.domain.common.TradeType;
import io.alw.datagen.DataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDate;
import java.util.Set;

@RecordBuilder
public record ConfirmationMatchStatus(
        long eventId,
        int eventVersion,
        long tradeId,
        int tradeVersion,
        Set<TradeLegMatchAttribute> tradeLegMatchAttributes,
        TradeType tradeType,
        MatchStatus matchStatus,
        LocalDate matchDate
//        LocalDateTime timeStamp
) implements DataGeneratable {
}
