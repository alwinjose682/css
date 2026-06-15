package io.alw.datagen.template;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record BuildItem<B, R>(
        Consumer<R> callback,
        Runnable runnableAfterCallback,
        Supplier<B> buildStep,
        Runnable runnable) {
    public BuildItem(Consumer<R> callback, Supplier<B> buildStep) {
        this(callback, null, buildStep, null);
    }

    public BuildItem(Supplier<B> buildStep) {
        this(null, null, buildStep, null);
    }

    public BuildItem(Consumer<R> callback, Runnable runnableAfterCallback, Supplier<B> buildStep) {
        this(callback, runnableAfterCallback, buildStep, null);
    }

    public BuildItem(Runnable runnable) {
        this(null, null, null, runnable);
    }
}
