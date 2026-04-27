package io.alw.css.refdtgtr.templates;

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
import io.alw.datagen.template.TemplateBuilder;

import java.time.LocalDateTime;

public final class CounterpartyNettingProfileTemplate extends TemplateBuilder<CounterpartyNettingProfile> {
    private final CounterpartyNettingProfileTemplateUtilities utilities;
    private final CounterpartyNettingProfileBuilder bdr;
    private final Counterparty counterparty;

    public CounterpartyNettingProfileTemplate(Counterparty counterparty) {
        this(counterparty, null);
    }

    private CounterpartyNettingProfileTemplate(Counterparty counterparty, CounterpartyNettingProfile parent) {
        super(parent);
        this.utilities = CounterpartyNettingProfileTemplateUtilities.singleton();
        this.bdr = CounterpartyNettingProfileBuilder.builder();
        this.counterparty = counterparty;
    }

    @Override
    public CounterpartyNettingProfileTemplate withDefaults() {
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

    public CounterpartyNettingProfileTemplate product(TradeType product) {
        bdr.product(product);
        return this;
    }

    public CounterpartyNettingProfileTemplate netByParentCounterpartyCode(boolean netByParentCounterpartyCode) {
        bdr.netByParentCounterpartyCode(netByParentCounterpartyCode);
        return this;
    }

    /// If netForAnyEntity is true, sets [CounterpartyNettingProfile#entityCode] as null. Else, assigns an entity code
    public CounterpartyNettingProfileTemplate netForAnyEntity(boolean netForAnyEntity) {
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
    public CounterpartyNettingProfile buildTemplate() {
        return bdr.build();
    }

    @Override
    protected CounterpartyNettingProfileTemplate childTemplate(CounterpartyNettingProfile parent) {
        throw new RuntimeException("Children types are not allowed for CounterpartyNettingProfile");
    }
}
