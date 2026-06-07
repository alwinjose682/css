package io.alw.css.fosimulator.template.model.amendmenttype;

import io.alw.css.fosimulator.template.model.AmendableFoCashMessageField;

import java.util.Set;
import java.util.function.Consumer;

public final class CounterpartyCode implements AmendableFoCashMessageFieldType {
    private Consumer<Set<AmendableFoCashMessageField>> action;

    @Override
    public Consumer<Set<AmendableFoCashMessageField>> action() {
        return action;
    }

    @Override
    public void setAction(Consumer<Set<AmendableFoCashMessageField>> action) {
        this.action = action;
    }
}
