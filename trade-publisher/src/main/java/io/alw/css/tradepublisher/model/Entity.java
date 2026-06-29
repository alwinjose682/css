package io.alw.css.tradepublisher.model;

public record Entity(
        String entityCode,
        String currCode,
        String countryCode,
        String bicCode
) {
}
