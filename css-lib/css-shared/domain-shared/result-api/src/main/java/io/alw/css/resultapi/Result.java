package io.alw.css.resultapi;

import java.util.function.Function;
import java.util.function.Supplier;

// NOTE: The 'Failure' type of 'Result<T>' has been removed. It makes sense only to have Success or Success kind of implementations of Result<T>.
// As usual, Failure must be handled by throwing exceptions.

public sealed interface Result<T> permits Outcome {
    Outcome<String> SUCCESS = new Outcome<>("Success");

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

    Result<T> andThen(Supplier<Result<T>> f);

    static <T> Result<T> of(Supplier<Result<T>> f) {
        return f.get();
    }
}
