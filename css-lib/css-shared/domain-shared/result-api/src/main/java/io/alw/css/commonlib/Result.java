package io.alw.css.commonlib;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/// [Result]'s [Failure] cannot be evaluated further.
///
/// Hence the [Failure#andThen(Supplier)] and [Failure#andThen(UnaryOperator)] chaining methods will NOT execute the next step of the sequence, instead it just returns the [Failure] instance itself.
///
/// NOTE: This API is not suitable if it is required to evaluate the [Failure] result in the next step.
public sealed interface Result<T> permits Success, Failure {

    <U> Result<U> andThenWith(Function<T, Result<U>> f);

    Result<?> andThen(UnaryOperator<Result<?>> f);

    /// Executes the consumer and returns 'this' to continue chaining methods
    Result<T> accept(Consumer<T> f);

    Result<?> andThen(Supplier<Result<?>> f);

    static <T> Result<T> of(Supplier<Result<T>> f) {
        return f.get();
    }
}
