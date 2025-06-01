package io.alw.css.resultapi;

import io.alw.css.domain.exception.CssException;

import java.util.function.Function;
import java.util.function.Supplier;

/// [Result]'s [Failure] cannot be evaluated further.
///
/// Ideally, the next steps of the sequence should not be executed when a [Failure] occurs.
/// But, it is not possible to do so. Therefore, all the methods of [Failure] just return itself.
/// Hence, NONE of the methods of [Failure] will execute the next step of the sequence.
public record Failure(CssException cssException) implements Result<CssException> {
    @Override
    public <U> Result<U> andThen(Function<CssException, Result<U>> f) {
        return (Result<U>) this;
    }

    @Override
    public Failure andThen(Supplier<Result<?>> f) {
        return this;
    }

//    @Override
//    public Failure andThen(UnaryOperator<Result<?>> f) {
//        return this;
//    }

    @Override
    public Failure andDo(ZeroFunction f) {
        return this;
    }
}
