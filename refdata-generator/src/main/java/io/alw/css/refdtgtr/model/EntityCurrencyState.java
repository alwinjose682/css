package io.alw.css.refdtgtr.model;

import io.alw.css.domain.referencedata.Currency;
import io.alw.css.domain.referencedata.Entity;

public record EntityCurrencyState(
        Entity entity,
        Currency currency,
        String state
) {
}
