package io.alw.css.fosimulator.template.model;

import java.util.*;

public final class AmendableFieldContext {
    private final Map<CashLegType, Set<AmendableFoCashMessageField>> amendableFields;

    public AmendableFieldContext() {
        amendableFields = new HashMap<>();
    }

    public AmendableFieldContext add(CashLegType cashLegType, AmendableFoCashMessageField field) {
        amendableFields.computeIfAbsent(cashLegType, _ -> new HashSet<>()).add(field);
        return this;
    }

    public AmendableFieldContext add(CashLegType cashLegType, AmendableFoCashMessageFieldSupplier supplier) {
        amendableFields.computeIfAbsent(cashLegType, _ -> new HashSet<>()).add(supplier);
        return this;
    }
}
