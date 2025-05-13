package io.alw.css.refdtgtr.model;

import io.alw.css.domain.referencedata.Country;
import io.alw.css.domain.referencedata.Currency;

import java.util.List;

public record GeneratedReferenceData(List<Country> countries,
                                     List<Currency> currencies,
                                     List<EntityAndDependentData> entityAndDependentData,
                                     List<CounterpartyAndDependentData> counterpartyAndDependentData) {
}
