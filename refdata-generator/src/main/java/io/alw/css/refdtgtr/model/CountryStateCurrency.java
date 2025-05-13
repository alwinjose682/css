package io.alw.css.refdtgtr.model;

import io.alw.datagen.TestDataGeneratable;
import io.alw.css.domain.referencedata.Country;
import io.alw.css.domain.referencedata.Currency;

/// NOTE: the field state can be used as the Country's state name or city name
public record CountryStateCurrency(
        Country country,
        String state,
        Currency currency
) implements TestDataGeneratable {
}
