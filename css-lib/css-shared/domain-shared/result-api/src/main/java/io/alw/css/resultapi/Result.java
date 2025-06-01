package io.alw.css.resultapi;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/// [Result]'s [Failure] cannot be evaluated further.
///
/// Hence the [Failure#andThen(Supplier)] and [Failure#andThen(UnaryOperator)] chaining methods will NOT execute the next step of the sequence, instead it just returns the [Failure] instance itself.
///
/// NOTE: This API is not suitable if it is required to evaluate the [Failure] result in the next step.
public sealed interface Result<T> permits Success, Failure {
    Success<String> SUCCESS = new Success<>("Success");

    <U> Result<U> andThen(Function<T, Result<U>> f);

//    Result<?> andThen(UnaryOperator<Result<?>> f);

    /// Executes the lambda. The implementations of [Result] returns itself('this') to continue chaining methods
    ///
    /// CAUTION:
    ///
    /// A subtle issue can happen and lead to bug when 'andDo' is used and later the consumer method is refactored:
    ///
    /// This method is intended to be used when there is no return value for the lambda being executed.
    /// But, if the consumer method is later refactored to return something and forgets to change the 'andDo' method to 'andThen',
    /// the returned value is silently ignored(compiler does not flag the error) and will NOT be used by the next step in the sequence.
    /// ie; The next step will use the old value from the previous step
    Result<T> andDo(ZeroFunction f);

    Result<?> andThen(Supplier<Result<?>> f);

    static <T> Result<T> of(Supplier<Result<T>> f) {
        return f.get();
    }
}
