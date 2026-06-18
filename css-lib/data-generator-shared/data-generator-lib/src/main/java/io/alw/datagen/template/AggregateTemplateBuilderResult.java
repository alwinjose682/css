package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.List;

public record AggregateTemplateBuilderResult<T extends DataGeneratable, R>(T root, List<R> grouped, List<R> related) {
}
