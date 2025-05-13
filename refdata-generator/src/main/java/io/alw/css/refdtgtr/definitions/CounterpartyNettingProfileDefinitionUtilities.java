package io.alw.css.refdtgtr.definitions;

import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.LongTokenGenerator;
import io.alw.datagen.definition.CountAware;

final class CounterpartyNettingProfileDefinitionUtilities implements CountAware {
    private static CounterpartyNettingProfileDefinitionUtilities utilities;

    private long counter;
    final LongTokenGenerator idGntr;
    final StringRefDataProvider stringRefDataProvider;
    final RefDataProvider refDataProvider;


    CounterpartyNettingProfileDefinitionUtilities() {
        this.counter = 0L;
        this.idGntr = new LongTokenGenerator(999L);
        this.stringRefDataProvider = new StringRefDataProvider();
        this.refDataProvider = new RefDataProvider();
    }

    static CounterpartyNettingProfileDefinitionUtilities singleton() {
        if (utilities == null) {
            synchronized (CounterpartyNettingProfileDefinitionUtilities.class) {
                if (utilities == null) {
                    utilities = new CounterpartyNettingProfileDefinitionUtilities();
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
