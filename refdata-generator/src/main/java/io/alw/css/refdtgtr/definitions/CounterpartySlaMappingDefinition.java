package io.alw.css.refdtgtr.definitions;

import io.alw.css.domain.referencedata.Counterparty;
import io.alw.css.domain.referencedata.CounterpartySlaMapping;
import io.alw.css.domain.referencedata.CounterpartySlaMappingBuilder;
import io.alw.css.domain.referencedata.Nostro;
import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.tokengenerator.LongTokenGenerator;
import io.alw.datagen.definition.BaseDefinition;

import java.time.LocalDateTime;

public class CounterpartySlaMappingDefinition extends BaseDefinition<CounterpartySlaMapping> {
    private final CounterpartySlaMappingDefinitionUtilities utilities;
    private final CounterpartySlaMappingBuilder bdr;
    private final Counterparty counterparty;
    private final Nostro nostro;

    public CounterpartySlaMappingDefinition(Counterparty counterparty, Nostro nostro) {
        this(counterparty, nostro, null);
    }

    private CounterpartySlaMappingDefinition(Counterparty counterparty, Nostro nostro, CounterpartySlaMapping parent) {
        super(parent);
        this.utilities = CounterpartySlaMappingDefinitionUtilities.singleton();
        this.bdr = CounterpartySlaMappingBuilder.builder();
        this.counterparty = counterparty;
        this.nostro = nostro;
    }

    @Override
    public CounterpartySlaMappingDefinition withDefaults() {
        LongTokenGenerator idGntr = utilities.idGntr;

        bdr
                .mappingID(idGntr.next())
                .mappingVersion(ConfigParams.FIRST_TEST_DATA_VERSION)
                .counterpartyCode(counterparty.counterpartyCode())
                .counterpartyVersion(counterparty.counterpartyVersion())
                .entityCode(nostro.entityCode())
                .currCode(nostro.currCode())
                .secondaryLedgerAccount(nostro.secondaryLedgerAccount())
                .active(true)
                .entryTime(LocalDateTime.now())
        ;
        return this;
    }

    @Override
    public CounterpartySlaMapping buildDefinition() {
        return bdr.build();
    }

    @Override
    protected CounterpartySlaMappingDefinition childDefinition(CounterpartySlaMapping parent) {
        throw new RuntimeException("Children types are not allowed for CounterpartySlaMapping");
    }
}
