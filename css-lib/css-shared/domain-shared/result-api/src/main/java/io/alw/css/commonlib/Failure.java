package io.alw.css.commonlib;

import io.alw.css.domain.exception.CssException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/// [Result]'s [Failure] cannot be evaluated further.
///
/// Hence the [Failure#andThen(Supplier)] and [Failure#andThen(UnaryOperator)] chaining methods will NOT execute the next step of the sequence, instead it just returns the [Failure] instance itself.
///
/// NOTE: This API is not suitable if it is required to evaluate the [Failure] result in the next step.
public record Failure(CssException cssException) implements Result<CssException> {
    @Override
    public <U> Result<U> andThenWith(Function<CssException, Result<U>> f) {
        return f.apply(cssException);
    }

    @Override
    public Failure andThen(Supplier<Result<?>> f) {
        return this;
    }

    @Override
    public Failure andThen(UnaryOperator<Result<?>> f) {
        return this;
    }

    @Override
    public Failure accept(Consumer<CssException> f) {
        f.accept(cssException);
        return this;
    }
}
