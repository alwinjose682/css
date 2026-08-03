package io.alw.css.tradepublisher.trade.model;

public record Entity(
        String entityCode,
        String currCode,
        String countryCode,
        String bicCode
) {
}
