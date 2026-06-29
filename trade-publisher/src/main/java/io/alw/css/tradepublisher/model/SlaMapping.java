package io.alw.css.tradepublisher.model;

public record SlaMapping(
        String entityCode,
        String currCode,
        String counterpartyCode,
        String secondaryLedgerAccount
) {
}
