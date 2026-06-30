package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface ChildBuildDirective<U extends DataGeneratable, UB> {
    record ChildBuildDirectiveType1<U extends DataGeneratable, UB>(
            Consumer<U> callback,
            Supplier<UB> buildStep) implements ChildBuildDirective<U, UB> {
    }

    record ChildBuildDirectiveType2<U extends DataGeneratable, UB>(
            Runnable runnable
    ) implements ChildBuildDirective<U, UB> {
    }

}
