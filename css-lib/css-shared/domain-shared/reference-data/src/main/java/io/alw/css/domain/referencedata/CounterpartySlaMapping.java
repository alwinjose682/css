package io.alw.css.domain.referencedata;

import io.alw.datagen.TestDataGeneratable;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/// Note on secondaryLedgerAccount/sla:
/// sla is NOT unique for a nostroID. sla can be changed by amending the nostro.
/// So, when default selection of nostroID is overridden due to the presence of this sla mapping, it must be verified that the nostroId selected based on sla belongs to the same entityCode and currCode defined by this mapping
///
/// Why not simply map counterparty directly to a nostroID instead of the sla?
/// The back office application I used to work for did not have such a simple mapping. Dont know about the complexities of designing and managing a reference data system
@RecordBuilder
public record CounterpartySlaMapping(
        long mappingID,
        int mappingVersion,
        @NotBlank String counterpartyCode,
        int counterpartyVersion,
        String entityCode,
        String currCode,
        String secondaryLedgerAccount,
        boolean active,
        LocalDateTime entryTime
) implements TestDataGeneratable {
}
