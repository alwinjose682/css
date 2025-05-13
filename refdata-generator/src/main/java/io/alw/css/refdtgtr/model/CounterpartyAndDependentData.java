package io.alw.css.refdtgtr.model;

import io.alw.css.domain.referencedata.Counterparty;
import io.alw.css.domain.referencedata.CounterpartyNettingProfile;
import io.alw.css.domain.referencedata.CounterpartySlaMapping;
import io.alw.css.domain.referencedata.Ssi;

import java.util.List;

public record CounterpartyAndDependentData(
        Counterparty counterparty,
        List<Ssi> ssis,
        List<CounterpartyNettingProfile> counterpartyNettingProfiles,
        List<CounterpartySlaMapping> counterpartySlaMappings
) {
}
