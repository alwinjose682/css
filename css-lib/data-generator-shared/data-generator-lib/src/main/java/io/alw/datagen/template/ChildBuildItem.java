package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record ChildBuildItem<U extends TestDataGeneratable, UB>(
        BiConsumer<? extends TestDataGeneratable, U> callback,
        Runnable runnableAfterCallback,
        Supplier<UB> buildStep) {
}
