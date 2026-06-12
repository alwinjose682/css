package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeLegType;

import java.util.*;

public final class AmendableFieldsCollection {
    private final Map<TradeLegType, Set<AmendableFoCashMessageField>> amendableFields;

    public AmendableFieldsCollection() {
        amendableFields = new HashMap<>();
    }

    public AmendableFieldsCollection add(TradeLegType tradeLegType, AmendableFoCashMessageField field) {
        amendableFields.computeIfAbsent(tradeLegType, _ -> new HashSet<>()).add(field);
        return this;
    }

    public AmendableFieldsCollection add(TradeLegType tradeLegType, AmendableFoCashMessageFieldSupplier supplier) {
        amendableFields.computeIfAbsent(tradeLegType, _ -> new HashSet<>()).add(supplier);
        return this;
    }

    public Set<AmendableFoCashMessageField> get(TradeLegType type) {
        return Collections.unmodifiableSet(amendableFields.get(type));
    }
}
