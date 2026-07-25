package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface ChildBuildDirective<U extends DataGeneratable, UB, P> {
    record ChildBuildDirectiveType1<U extends DataGeneratable, UB, P>(
            Consumer<U> callback,
            Supplier<UB> buildStep) implements ChildBuildDirective<U, UB, P> {
    }

    record ChildBuildDirectiveType2<U extends DataGeneratable, UB, P>(
            Runnable runnable
    ) implements ChildBuildDirective<U, UB, P> {
    }

    record ChildBuildDirectiveType3<U extends DataGeneratable, UB, P>(
            Supplier<P> buildStepParamSupplier,
            Function<P, UB> buildStep,
            BiConsumer<P, U> callback) implements ChildBuildDirective<U, UB, P> {
    }
}
