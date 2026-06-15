package io.alw.datagen.template;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ChildBuildItem<B, R>(
        Consumer<R> callback,
        Runnable runnableAfterCallback,
        Supplier<B> buildStep,
        Runnable runnable) {
    public ChildBuildItem(Consumer<R> callback, Supplier<B> buildStep) {
        this(callback, null, buildStep, null);
    }

    public ChildBuildItem(Supplier<B> buildStep) {
        this(null, null, buildStep, null);
    }

    public ChildBuildItem(Consumer<R> callback, Runnable runnableAfterCallback, Supplier<B> buildStep) {
        this(callback, runnableAfterCallback, buildStep, null);
    }

    public ChildBuildItem(Runnable runnable) {
        this(null, null, null, runnable);
    }
}
