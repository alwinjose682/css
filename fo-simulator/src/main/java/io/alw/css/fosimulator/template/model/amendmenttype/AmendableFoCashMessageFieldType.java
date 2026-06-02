package io.alw.css.fosimulator.template.model.amendmenttype;

import io.alw.css.fosimulator.template.model.AmendableFoCashMessageField;

import java.util.Set;
import java.util.function.Consumer;

public sealed interface AmendableFoCashMessageFieldType permits Amount, CounterpartyCode, ValueDate {
    Consumer<Set<AmendableFoCashMessageField>> action();
    void setAction(Consumer<Set<AmendableFoCashMessageField>> action);
}
