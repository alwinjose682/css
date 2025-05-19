package io.alw.css.refdtgtr.definitions;

import io.alw.css.domain.cashflow.TradeType;
import io.alw.css.domain.common.CssNettingType;
import io.alw.css.domain.referencedata.Counterparty;
import io.alw.css.domain.referencedata.CounterpartyNettingProfile;
import io.alw.css.domain.referencedata.CounterpartyNettingProfileBuilder;
import io.alw.css.domain.referencedata.Entity;
import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.css.refdtgtr.model.TestDataType;
import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.LongTokenGenerator;
import io.alw.datagen.definition.BaseDefinition;

import java.time.LocalDateTime;

public final class CounterpartyNettingProfileDefinition extends BaseDefinition<CounterpartyNettingProfile> {
    private final CounterpartyNettingProfileDefinitionUtilities utilities;
    private final CounterpartyNettingProfileBuilder bdr;
    private final Counterparty counterparty;

    public CounterpartyNettingProfileDefinition(Counterparty counterparty) {
        this(counterparty, null);
    }

    private CounterpartyNettingProfileDefinition(Counterparty counterparty, CounterpartyNettingProfile parent) {
        super(parent);
        this.utilities = CounterpartyNettingProfileDefinitionUtilities.singleton();
        this.bdr = CounterpartyNettingProfileBuilder.builder();
        this.counterparty = counterparty;
    }

    @Override
    public CounterpartyNettingProfileDefinition withDefaults() {
        LongTokenGenerator idGntr = utilities.idGntr;
        StringRefDataProvider stringRefDataProvider = utilities.stringRefDataProvider;

        bdr
                .nettingProfileID(idGntr.next())
                .nettingProfileVersion(ConfigParams.FIRST_TEST_DATA_VERSION)
                .counterpartyCode(counterparty.counterpartyCode())
                .counterpartyVersion(counterparty.counterpartyVersion())
                .nettingType(CssNettingType.valueOf(stringRefDataProvider.next(TestDataType.CSS_NETTING_TYPE)))
                .active(true)
                .entryTime(LocalDateTime.now())
        ;
        return this;
    }

    public CounterpartyNettingProfileDefinition product(TradeType product) {
        bdr.product(product);
        return this;
    }

    public CounterpartyNettingProfileDefinition netByParentCounterpartyCode(boolean netByParentCounterpartyCode) {
        bdr.netByParentCounterpartyCode(netByParentCounterpartyCode);
        return this;
    }

    /// If netForAnyEntity is true, sets [CounterpartyNettingProfile#entityCode] as null. Else, assigns an entity code
    public CounterpartyNettingProfileDefinition netForAnyEntity(boolean netForAnyEntity) {
        bdr.netForAnyEntity(netForAnyEntity);
        if (netForAnyEntity) {
            bdr.entityCode(null);
        } else {
            RefDataProvider refDataProvider = utilities.refDataProvider;
            Entity entity = (Entity) refDataProvider.next(TestDataType.ENTITY);
            bdr.entityCode(entity.entityCode());
        }
        return this;
    }

    @Override
    public CounterpartyNettingProfile buildDefinition() {
        return bdr.build();
    }

    @Override
    protected CounterpartyNettingProfileDefinition childDefinition(CounterpartyNettingProfile parent) {
        throw new RuntimeException("Children types are not allowed for CounterpartyNettingProfile");
    }
}
