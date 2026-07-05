package io.alw.css.confirmation;

public record TradeLegMatchAttribute(
        long tradeLegId,
        int tradeLegVersion,
        String nostroId,
        String ssiId
) {
}
