package io.alw.css.resultapi;

import java.util.function.Function;
import java.util.function.Supplier;

public record Success<T>(T t) implements Result<T> {

    @Override
    public <U> Result<U> andThen(Function<T, Result<U>> f) {
        return f.apply(t);
    }

//    @Override
//    public Result<?> andThen(UnaryOperator<Result<?>> f) {
//        return f.apply(this);
//    }

    @Override
    public Success<T> andDo(ZeroFunction f) {
        return this;
    }

    @Override
    public Result<T> andThen(Supplier<Result<T>> f) {
        return f.get();
    }
}
