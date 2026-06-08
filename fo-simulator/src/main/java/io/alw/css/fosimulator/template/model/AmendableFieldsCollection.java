package io.alw.css.fosimulator.template.model;

import io.alw.css.fosimulator.template.domain.CashLegType;

import java.util.*;

public final class AmendableFieldsCollection {
    private final Map<CashLegType, Set<AmendableFoCashMessageField>> amendableFields;

    public AmendableFieldsCollection() {
        amendableFields = new HashMap<>();
    }

    public AmendableFieldsCollection add(CashLegType cashLegType, AmendableFoCashMessageField field) {
        amendableFields.computeIfAbsent(cashLegType, _ -> new HashSet<>()).add(field);
        return this;
    }

    public AmendableFieldsCollection add(CashLegType cashLegType, AmendableFoCashMessageFieldSupplier supplier) {
        amendableFields.computeIfAbsent(cashLegType, _ -> new HashSet<>()).add(supplier);
        return this;
    }

    public Set<AmendableFoCashMessageField> get(CashLegType type) {
        return Collections.unmodifiableSet(amendableFields.get(type));
    }
}
