package io.alw.css.tradepublisher.trade.model;

public record SlaMapping(
        String entityCode,
        String currCode,
        String counterpartyCode,
        String secondaryLedgerAccount
) {
}
