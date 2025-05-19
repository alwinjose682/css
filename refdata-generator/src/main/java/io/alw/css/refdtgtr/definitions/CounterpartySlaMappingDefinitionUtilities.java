package io.alw.css.refdtgtr.definitions;

import io.alw.datagen.tokengenerator.LongTokenGenerator;
import io.alw.datagen.definition.CountAware;

public final class CounterpartySlaMappingDefinitionUtilities implements CountAware {
    private static CounterpartySlaMappingDefinitionUtilities utilities;

    private long counter;
    final LongTokenGenerator idGntr;

    CounterpartySlaMappingDefinitionUtilities() {
        this.counter = 0L;
        this.idGntr = new LongTokenGenerator(100L);
    }

    static CounterpartySlaMappingDefinitionUtilities singleton() {
        if (utilities == null) {
            synchronized (CounterpartySlaMappingDefinitionUtilities.class) {
                if (utilities == null) {
                    utilities = new CounterpartySlaMappingDefinitionUtilities();
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
