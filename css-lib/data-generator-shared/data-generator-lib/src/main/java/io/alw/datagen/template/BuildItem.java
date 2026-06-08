package io.alw.datagen.template;

import java.util.function.Consumer;
import java.util.function.Supplier;

record BuildItem<B, R>(Consumer<R> callback, Runnable runnableAfterCallback, Supplier<B> buildStep, Runnable runnable) {
    BuildItem(Consumer<R> callback, Supplier<B> buildStep) {
        this(callback, null, buildStep, null);
    }

    BuildItem(Supplier<B> buildStep) {
        this(null, null, buildStep, null);
    }

    BuildItem(Consumer<R> callback, Runnable runnableAfterCallback, Supplier<B> buildStep) {
        this(callback, runnableAfterCallback, buildStep, null);
    }

    BuildItem(Runnable runnable) {
        this(null, null, null, runnable);
    }
}
