package io.alw.css.fosimulator.template.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AmendableFieldContext {
    private final Map<CashLegType, Set<AmendableFoCashMessageFieldAndValueApplier>> amendableFields;

    public AmendableFieldContext() {
        amendableFields = new HashMap<>();
    }

    public AmendableFieldContext add(CashLegType cashLegType, AmendableFoCashMessageField field) {
        var applier = new AmendableFieldValueApplier.DirectApplier();
        var fieldAndApplier = new AmendableFoCashMessageFieldAndValueApplier(field, applier);
        amendableFields.computeIfAbsent(cashLegType, _ -> new HashSet<>()).add(fieldAndApplier);
        return this;
    }

    public AmendableFieldContext add(CashLegType cashLegType, AmendableFoCashMessageField field, AmendableFieldValueApplier applier) {
        var fieldAndApplier = new AmendableFoCashMessageFieldAndValueApplier(field, applier);
        amendableFields.computeIfAbsent(cashLegType, _ -> new HashSet<>()).add(fieldAndApplier);
        return this;
    }
}
