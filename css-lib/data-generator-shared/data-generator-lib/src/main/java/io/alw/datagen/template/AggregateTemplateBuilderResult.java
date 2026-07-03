package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.List;

public record AggregateTemplateBuilderResult<T extends DataGeneratable>(
        T result,
        List<T> childResults) {
}
