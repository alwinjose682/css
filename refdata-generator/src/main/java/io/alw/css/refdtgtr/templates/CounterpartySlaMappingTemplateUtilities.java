package io.alw.css.refdtgtr.templates;

import io.alw.datagen.tokengenerator.LongTokenGenerator;
import io.alw.datagen.template.CountAware;

public final class CounterpartySlaMappingTemplateUtilities implements CountAware {
    private static CounterpartySlaMappingTemplateUtilities utilities;

    private long counter;
    final LongTokenGenerator idGntr;

    CounterpartySlaMappingTemplateUtilities() {
        this.counter = 0L;
        this.idGntr = new LongTokenGenerator(100L);
    }

    static CounterpartySlaMappingTemplateUtilities singleton() {
        if (utilities == null) {
            synchronized (CounterpartySlaMappingTemplateUtilities.class) {
                if (utilities == null) {
                    utilities = new CounterpartySlaMappingTemplateUtilities();
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
