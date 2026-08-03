package io.alw.css.confirmation;

import java.time.LocalDate;

public record TradeLegMatchAttribute(
        long tradeLegId,
        int tradeLegVersion,
        String nostroId,
        String ssiId,
        LocalDate valueDate
) {
}
