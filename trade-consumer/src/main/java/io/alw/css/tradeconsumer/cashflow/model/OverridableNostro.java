package io.alw.css.tradeconsumer.cashflow.model;

public record OverridableNostro(
        // Counterparty Sla Mapping Details
        long slaMappingID,
        int slaMappingVersion,
        String counterpartyCode,
        boolean activeMapping,
        // Standard Nostro Details
        String nostroID,
        int nostroVersion,
        String entityCode,
        String currCode,
        String secondaryLedgerAccount,
        boolean primary,
        boolean activeNostro
) {
}
