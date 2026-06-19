package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.Set;

public record AggregateTemplateBuilderResult<T extends DataGeneratable>(
        T result,
        Set<T> childResults) {
}
