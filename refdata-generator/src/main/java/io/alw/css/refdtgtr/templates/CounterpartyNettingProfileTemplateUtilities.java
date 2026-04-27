package io.alw.css.refdtgtr.templates;

import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.LongTokenGenerator;
import io.alw.datagen.template.CountAware;

final class CounterpartyNettingProfileTemplateUtilities implements CountAware {
    private static CounterpartyNettingProfileTemplateUtilities utilities;

    private long counter;
    final LongTokenGenerator idGntr;
    final StringRefDataProvider stringRefDataProvider;
    final RefDataProvider refDataProvider;


    CounterpartyNettingProfileTemplateUtilities() {
        this.counter = 0L;
        this.idGntr = new LongTokenGenerator(999L);
        this.stringRefDataProvider = new StringRefDataProvider();
        this.refDataProvider = new RefDataProvider();
    }

    static CounterpartyNettingProfileTemplateUtilities singleton() {
        if (utilities == null) {
            synchronized (CounterpartyNettingProfileTemplateUtilities.class) {
                if (utilities == null) {
                    utilities = new CounterpartyNettingProfileTemplateUtilities();
                }
            }
        }
        utilities.incrementCounter();
        return utilities;
    }

    @Override
    public long counter() {
        return counter;
    }

    @Override
    public void incrementCounter() {
        ++counter;
    }
}
