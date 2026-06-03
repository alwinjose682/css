package io.alw.css.fosimulator.template.model;

public record AmendableFoCashMessageFieldAndValueApplier(
        AmendableFoCashMessageField field,
        AmendableFieldValueApplier action
) {
}
