package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record ChildBuildItem<U extends DataGeneratable, UB>(
        BiConsumer<? extends DataGeneratable, U> callback,
        Runnable runnableAfterCallback,
        Supplier<UB> buildStep) {
}
