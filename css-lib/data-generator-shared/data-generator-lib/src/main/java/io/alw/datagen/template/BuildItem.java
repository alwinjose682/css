package io.alw.datagen.template;

import java.util.function.Consumer;
import java.util.function.Supplier;

record BuildItem<B, R>(Consumer<R> callback, Supplier<B> buildStep, Runnable runnable) {
    BuildItem(Supplier<B> buildStep) {
        this(null, buildStep, null);
    }

    BuildItem(Consumer<R> callback, Supplier<B> buildStep) {
        this(callback, buildStep, null);
    }

    BuildItem(Runnable runnable) {
        this(null, null, runnable);
    }
}
