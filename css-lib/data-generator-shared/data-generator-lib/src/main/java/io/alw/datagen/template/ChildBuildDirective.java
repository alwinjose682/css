package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ChildBuildDirective<U extends DataGeneratable, UB>(
        Consumer<U> callback,
        Runnable runnableAfterCallback,
        Supplier<UB> buildStep) {
}
