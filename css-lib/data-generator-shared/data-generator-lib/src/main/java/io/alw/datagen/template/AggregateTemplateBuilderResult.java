package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.List;

public record AggregateTemplateBuilderResult<T extends TestDataGeneratable, R>(T root, List<R> grouped, List<R> related) {
}
