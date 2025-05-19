package io.alw.datagen.tokengenerator;

public sealed interface TokenGenerator<T> permits LongTokenGenerator, AlphaNumericTokenGenerator, IdentityStringTokenGenerator, BinaryStringTokenGenerator {

    T next();

    T current();

    default String nextAsString() {
        return String.valueOf(next());
    }

    default String currentAsString() {
        return String.valueOf(current());
    }
}
