package io.alw.css.commonlib;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public record Success<T>(T t) implements Result<T> {

    @Override
    public <U> Result<U> andThenWith(Function<T, Result<U>> f) {
        return f.apply(t);
    }

    @Override
    public Result<?> andThen(UnaryOperator<Result<?>> f) {
        return f.apply(this);
    }

    @Override
    public Success<T> accept(Consumer<T> f) {
        f.accept(t);
        return this;
    }

    @Override
    public Result<?> andThen(Supplier<Result<?>> f) {
        return f.get();
    }
}
